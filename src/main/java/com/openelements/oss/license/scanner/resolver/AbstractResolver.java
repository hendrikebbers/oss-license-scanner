package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.SourceforgeClient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(AbstractResolver.class);

    final GitHubClient gitHubClient;

    public AbstractResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    protected <T> T installLocally(final String repositoryUrl, String version, Function<Path, T> handler) {
        final String tag = gitHubClient.findMatchingTag(repositoryUrl, version)
                .orElseThrow(() -> new RuntimeException("No tag found for version: " + version));
        final Path pathToProject = gitHubClient.downloadTag(repositoryUrl, tag);
        try {
            return handler.apply(pathToProject);
        } finally {
            if(pathToProject != null) {
                try {
                    try (Stream<Path> paths = Files.walk(pathToProject)) {
                        paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                    }
                } catch (IOException e) {
                    log.error("Error in deleting temporary directory '" + pathToProject + "'", e);
                }
            }
        }
    }

    protected Optional<License> getLicenseFromGitHub(String repository) {
        try {
            return gitHubClient.getLicense(repository);
        } catch (Exception e) {
            log.error("Failed to get license from GitHub for {}", repository, e);
            return Optional.empty();
        }
    }

    protected Optional<License> getLicenseFromSourceforge(String repository) {
        if(repository.startsWith("https://sourceforge.net/projects/")) {
            final String project = repository.substring("https://sourceforge.net/projects/".length());
            return SourceforgeClient.call(project);
        }
        if(repository.endsWith(".sourceforge.io") && repository.startsWith("https://")) {
            final String project = repository.substring("https://".length(), repository.length() - ".sourceforge.io".length());
            return SourceforgeClient.call(project);
        }
        log.warn("Bad sourceforge url: {}", repository);
        return Optional.empty();
    }

    protected Optional<License> getLicenseFromProjectUrl(String projectUrl) {
        if(projectUrl == null) {
            return Optional.empty();
        }
        if(projectUrl.contains("github")) {
            return getLicenseFromGitHub(projectUrl);
        }
        if(projectUrl.contains("sourceforge")) {
            return getLicenseFromSourceforge(projectUrl);
        }
        log.warn("No license resolver for {}", projectUrl);
        return Optional.empty();
    }

    protected GitHubClient getGitHubClient() {
        return gitHubClient;
    }
}
