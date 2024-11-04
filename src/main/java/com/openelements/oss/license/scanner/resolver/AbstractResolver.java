package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Identifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
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
        final Path pathToProject = gitHubClient.download(repositoryUrl, tag);
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

    protected GitHubClient getGitHubClient() {
        return gitHubClient;
    }
}
