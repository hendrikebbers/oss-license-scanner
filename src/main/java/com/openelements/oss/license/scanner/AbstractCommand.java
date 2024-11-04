package com.openelements.oss.license.scanner;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import de.siegmar.fastcsv.writer.CsvWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

public abstract class AbstractCommand implements Callable<Integer> {

    @Option(names = {"-n", "--name"}, description = "the name of the library")
    private String name;

    @Option(names = {"-r", "--repository"}, description = "the repository url of the library")
    private String repositoryUrl;

    @Option(names = {"-l", "--local"}, description = "the local path to the project")
    private String localPath;


    @Option(names = {"-v", "--version"}, description = "the version of the library")
    private String version;

    @Option(names = {"-t", "--token"}, description = "the GitHub token that is used to authenticate with the GitHub API")
    private String token;

    @Option(names = {"-m", "--manual"}, description = "a csv file containing the manual dependency to license mapping")
    private String manualCsv;

    @Override
    public Integer call() {
        if(repositoryUrl == null && localPath == null && name == null) {
            System.err.println("Either name (-n), repository (-r) or local path (-l) must be provided");
            return ExitCode.USAGE;
        }
        if(repositoryUrl != null && (localPath != null || name != null)) {
            System.err.println("Exactly one of name (-n), repository (-r) or local path (-l) must be provided");
            return ExitCode.USAGE;
        }
        if(localPath != null && (repositoryUrl != null || name != null)) {
            System.err.println("Exactly one of name (-n), repository (-r) or local path (-l) must be provided");
            return ExitCode.USAGE;
        }
        if(name != null && (repositoryUrl != null || localPath != null)) {
            System.err.println("Exactly one of name (-n), repository (-r) or local path (-l) must be provided");
            return ExitCode.USAGE;
        }
        if(name != null && version == null) {
            System.err.println("Version (-v) must be provided");
            return ExitCode.USAGE;
        }
        try {
            if(manualCsv != null) {
                Path file = Paths.get(manualCsv);
                final String languageType = getLanguageType();
                try (CsvReader<NamedCsvRecord> csv = CsvReader.builder().ofNamedCsvRecord(file)) {
                    csv.forEach(rec -> {
                        final String type = rec.getField("type");
                        if(Objects.equals(type, languageType)) {
                            final String name = rec.getField("name");
                            final String version = rec.getField("version");
                            final Identifier identifier = new Identifier(name, version);
                            final String licenseName = rec.getField("license");
                            final String licenseUrl = rec.getField("license-url");
                            final String licenseSource = rec.getField("license-source");
                            final License license = new License(licenseName, licenseUrl, licenseSource);
                            LicenseCache.getInstance().addLicense(identifier, license);
                        }
                    });
                }
            }
            final GitHubClient gitHubClient = new GitHubClient(token);
            final Resolver resolver = createResolver(gitHubClient);
            final Set<Dependency> dependencies = new HashSet<>();
            if(localPath != null) {
                final Path pathToProject = Path.of(localPath);
                dependencies.addAll(resolver.resolve(pathToProject));
            } else if(repositoryUrl != null) {
                final Path pathToProject;
                if(version == null) {
                    pathToProject = gitHubClient.downloadLatest(repositoryUrl);
                } else {
                    final String tag = gitHubClient.findMatchingTag(repositoryUrl, version)
                            .orElseThrow(() -> new RuntimeException("No tag found for version: " + version));
                    pathToProject = gitHubClient.downloadTag(repositoryUrl, tag);
                }
                try {
                    dependencies.addAll(resolver.resolve(pathToProject));
                } finally {
                    if(pathToProject != null) {
                        try {
                            try (Stream<Path> paths = Files.walk(pathToProject)) {
                                paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Error in deleting temporary directory '" + pathToProject + "'", e);
                        }
                    }
                }
            } else {
                final Identifier identifier = new Identifier(name, version);
                dependencies.addAll(resolver.resolve(identifier));
            }
            final PrintWriter writer = new PrintWriter(System.out);
            try (CsvWriter csv = CsvWriter.builder().build(writer)) {
                csv.writeRecord("name", "version", "repository", "license", "license-url", "license-source");
                dependencies.stream()
                        .forEach(d -> csv.writeRecord(d.identifier().name(), d.identifier().version(), d.repository(), d.license().name(), d.license().url(), d.license().source()));
            }
        } catch (Exception e) {
            System.err.println("Error fetching dependencies for " + repositoryUrl + ":" + version);
            e.printStackTrace();
            return ExitCode.SOFTWARE;
        }
        return ExitCode.OK;
    }

    protected abstract Resolver createResolver(GitHubClient client);

    protected abstract String getLanguageType();
}