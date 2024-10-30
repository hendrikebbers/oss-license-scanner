package com.openelements.oss.license.git;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.data.License;
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

    public GitHubClient() {
        githubToken = System.getenv(GITHUB_TOKEN);
        if(githubToken != null) {
            log.info("Using {} environment variable for GitHub API authentication", githubToken);
        } else {
            log.warn("{} environment variable not found, GitHub API rate limits will apply", GITHUB_TOKEN);
        }

        client = HttpClient.newHttpClient();
    }

    private record Repository(String owner, String repo) {
    }

    public static Repository parseRepository(String githubUrl) {
        if(githubUrl.endsWith(".git")) {
            return parseRepository(githubUrl.substring(0, githubUrl.length() - 4));
        }
        Pattern pattern = Pattern.compile("https://github.com/([^/]+)/([^/]+)");
        Matcher matcher = pattern.matcher(githubUrl);
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

    private final Map<String, License> licenseCache = new ConcurrentHashMap<>();

    public License getLicense(String repositoryUrl) {
        if(licenseCache.containsKey(repositoryUrl)) {
            return licenseCache.get(repositoryUrl);
        }
        try {
            log.info("Getting license for repository: {}", repositoryUrl);
            final Repository repository = parseRepository(repositoryUrl);
            final String apiUrl = String.format("repos/%s/%s", repository.owner, repository.repo);
            final HttpResponse<String>  response = sendRequest(GITHUB_API_URL + "/" + apiUrl);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get repository info: " + response.body());
            }
            final String body = response.body();
            final JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            if(jsonObject.has("license") && !jsonObject.get("license").isJsonNull()) {
                JsonObject licenseObject = jsonObject.get("license").getAsJsonObject();
                final String name = getOrDefault(licenseObject, "name", "Unknown");
                final String url = getOrDefault(licenseObject, "url", "Unknown");
                License license = new License(name, url);
                licenseCache.put(repositoryUrl, license);
                return license;
            }
            licenseCache.put(repositoryUrl, License.UNKNOWN);
            return License.UNKNOWN;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get license for repository: " + repositoryUrl, e);
        }
    }

    public boolean existsTag(String repositoryUrl, String tag) {
        try {
            log.info("Checking if tag '{}' exists for repository: {}", tag, repositoryUrl);
            final Repository repository = parseRepository(repositoryUrl);
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

    public Path download(String repositoryUrl, String tag) {
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
