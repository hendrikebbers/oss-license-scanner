package com.openelements.oss.license.scanner.clients;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.api.ApiConstants;
import com.openelements.oss.license.scanner.api.License;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubClient {

    private final static Logger log = LoggerFactory.getLogger(GitHubClient.class);
    public static final String GITHUB_TOKEN = "GITHUB_TOKEN";
    public static final String GITHUB_API_URL = "https://api.github.com";
    public static final String HTTP_ACCEPT_HEADER = "Accept";
    public static final String GITHUB_V_3_JSON = "application/vnd.github.v3+json";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final String githubToken;

    private final HttpClient client;

    public GitHubClient(String githubToken) {
        if(githubToken == null) {
            final String tokenFromEnv = System.getenv(GITHUB_TOKEN);
            if(tokenFromEnv != null) {
                this.githubToken = tokenFromEnv;
            } else {
                log.warn("{} environment variable not found, GitHub API rate limits will apply", GITHUB_TOKEN);
                this.githubToken = null;
            }
        } else {
            this.githubToken = githubToken;
        }
        client = HttpClient.newHttpClient();
    }

    private record Repository(String owner, String repo) {
    }

    public static String normalizeUrl(String githubUrl) {
        if(githubUrl == null) {
            return null;
        }
        if(githubUrl.startsWith("git:git@github.com:")) {
            return normalizeUrl("https://github.com/" + githubUrl.substring(19));
        }
        if(githubUrl.startsWith("git@github.com:")) {
            return normalizeUrl("https://github.com/" + githubUrl.substring(15));
        }
        if(githubUrl.endsWith(".git")) {
            return normalizeUrl(githubUrl.substring(0, githubUrl.length() - 4));
        }
        if(githubUrl.startsWith("git+")) {
            return normalizeUrl(githubUrl.substring(4));
        }
        if(githubUrl.startsWith("git:git://")) {
            return normalizeUrl("https://" + githubUrl.substring(10));
        }
        if(githubUrl.startsWith("git://")) {
            return normalizeUrl("https://" + githubUrl.substring(6));
        }
        if(githubUrl.startsWith("ssh://git@")) {
            return normalizeUrl("https://" + githubUrl.substring(10));
        }
        if(githubUrl.startsWith("scm:")) {
            return normalizeUrl(githubUrl.substring(4));
        }
        if(githubUrl.startsWith("http://github.com")) {
            return normalizeUrl("https://github.com" + githubUrl.substring(17));
        }
        if(githubUrl.startsWith("git@github.com")) {
            return normalizeUrl("https://github.com" + githubUrl.substring(14));
        }
        if(githubUrl.endsWith("#main")) {
            return normalizeUrl(githubUrl.substring(0, githubUrl.length() - 5));
        }
        if(githubUrl.startsWith("https://@github.com/")) {
            return normalizeUrl("https://github.com/" + githubUrl.substring(23));
        }
        if(githubUrl.startsWith("github.com:")) {
            return normalizeUrl("https://github.com/" + githubUrl.substring(11));
        }
        return githubUrl;
    }

    public static Repository parseRepository(String githubUrl) {
        final String normalizedUrl = normalizeUrl(githubUrl);
        Pattern pattern = Pattern.compile("https://github.com/([^/]+)/([^/]+)");
        Matcher matcher = pattern.matcher(normalizedUrl);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            return new Repository(owner, repo);
        }
       throw new IllegalArgumentException("Invalid GitHub URL: " + githubUrl);
    }

    private HttpRequest createRequest(String url) throws URISyntaxException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header(HTTP_ACCEPT_HEADER, GITHUB_V_3_JSON);
        if(githubToken != null) {
            builder.header(AUTHORIZATION_HEADER, "token " + githubToken);
        }
        return builder.build();
    }

    private HttpResponse<String> sendRequest(String url) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = createRequest(url);
        final HttpResponse<String> response =  client.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 301 || response.statusCode() == 302 || response.statusCode() == 307) {
            final String location = response.headers().firstValue("location").orElseThrow();
            return sendRequest(location);
        }
        return response;
    }

    public Optional<License> getLicense(String repositoryUrl) {
        if(repositoryUrl == null) {
            return Optional.empty();
        }
        final Repository repository = parseRepository(repositoryUrl);
        log.info("Getting license for repository {}/{}", repository.owner, repository.repo);
        try {
            final String apiUrl = String.format("repos/%s/%s", repository.owner, repository.repo);
            final String requestUrl = GITHUB_API_URL + "/" + apiUrl;
            final HttpResponse<String>  response = sendRequest(requestUrl);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get repository info for {}/{}: " + response.body());
            }
            final String body = response.body();
            final JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            if(jsonObject.has("license") && !jsonObject.get("license").isJsonNull()) {
                JsonObject licenseObject = jsonObject.get("license").getAsJsonObject();
                final String name = getOrDefault(licenseObject, "name", ApiConstants.UNKNOWN);
                final String url = getOrDefault(licenseObject, "url", ApiConstants.UNKNOWN);
                License license = new License(name, url, requestUrl);
                return Optional.of(license);
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get license for repository: " + repository.owner + "/" + repository.repo, e);
        }
    }

    public Optional<String> findMatchingTag(String repositoryUrl, String tag) {
        if (existsTag(repositoryUrl, tag)) {
            return Optional.of(tag);
        } else if (tag.startsWith("v") && existsTag(repositoryUrl, tag.substring(1))) {
            return Optional.of(tag.substring(1));
        } else if (existsTag(repositoryUrl, "v" + tag)) {
            return Optional.of("v" + tag);
        }
        return Optional.empty();
    }

    public boolean existsTag(String repositoryUrl, String tag) {
        try {
            final Repository repository = parseRepository(repositoryUrl);
            log.info("Checking if tag '{}' exists for repository: {}/{}", tag, repository.owner, repository.repo);

            final String apiUrl = String.format("repos/%s/%s/tags", repository.owner, repository.repo);
            final HttpResponse<String>  response = sendRequest(GITHUB_API_URL + "/" + apiUrl);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get repository info: " + response.body());
            }
            final String body = response.body();
            final JsonElement jsonElement = JsonParser.parseString(body);

           return jsonElement.getAsJsonArray().asList().stream()
                    .map(elem -> elem.getAsJsonObject().get("name").getAsString())
                    .filter(t -> t.equals(tag))
                    .findAny()
                    .isPresent();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get tags for repository: " + repositoryUrl, e);
        }
    }

    public Path downloadTag(String repositoryUrl, String tag) {
        try {
            log.info("Downloading repository: {}", repositoryUrl);
            final Repository repository = parseRepository(repositoryUrl);

            final String url = String.format("https://github.com/%s/%s/archive/refs/tags/%s.zip", repository.owner,
                    repository.repo, tag);
            final Path zipFilePath = download(url);
            final Path tempDirectory = unzip(zipFilePath.toFile().getAbsolutePath());
            return tempDirectory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download repository: " + repositoryUrl, e);
        }
    }

    public Path downloadLatest(String repositoryUrl) {
        try {
            log.info("Downloading repository: {}", repositoryUrl);
            final Repository repository = parseRepository(repositoryUrl);
            final String url = String.format("https://github.com/%s/%s/archive/refs/heads/main.zip", repository.owner,
                    repository.repo);
            final Path zipFilePath = download(url);
            final Path tempDirectory = unzip(zipFilePath.toFile().getAbsolutePath());
            return tempDirectory;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download repository: " + repositoryUrl, e);
        }
    }


    public static Path download(String fileUrl) throws IOException {
        final Path tempDirectory = Files.createTempDirectory("license-scanner");
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        Path outputPath = Paths.get(tempDirectory.toFile().getAbsolutePath(), fileName);
        try (InputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile())) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
        return outputPath;
    }

    private static Path unzip(String zipFilePath) throws IOException {
        Path tempDir = Files.createTempDirectory("unzipped_repo");
        Path rootSubfolder = null;

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(zipFilePath)))) {
            ZipEntry entry = zipIn.getNextEntry();

            // Iterate over entries in the ZIP file
            while (entry != null) {
                Path filePath = tempDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    if (rootSubfolder == null) {
                        // Capture the root folder (first top-level directory in the ZIP)
                        rootSubfolder = tempDir.resolve(entry.getName());
                    }
                    Files.createDirectories(filePath);
                } else {
                    // Ensure parent directories exist
                    Files.createDirectories(filePath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                        byte[] bytesIn = new byte[1024];
                        int read;
                        while ((read = zipIn.read(bytesIn)) != -1) {
                            fos.write(bytesIn, 0, read);
                        }
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        return rootSubfolder;
    }

    private String getOrDefault(JsonObject jsonObject, String key, String defaultValue) {
        return jsonObject.has(key) && !jsonObject.get(key).isJsonNull()
                ? jsonObject.get(key).getAsString()
                : defaultValue;
    }
}
