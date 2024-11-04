package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.MavenCentralClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJavaResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(AbstractJavaResolver.class);

    private final GitHubClient gitHubClient;

    public AbstractJavaResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public final Set<Dependency> resolve(Identifier identifier) {
        //TODO: extract later
        final String repositoryUrl = identifier.name();
        final String tag = gitHubClient.findMatchingTag(repositoryUrl, identifier.version())
                .orElseThrow(() -> new RuntimeException("No tag found for version: " + identifier.version()));
        final Path pathToProject = gitHubClient.download(repositoryUrl, tag);
        return getDependenciesFromProject(pathToProject);
    }

    protected abstract Set<Dependency> getDependenciesFromProject(Path pathToProject);

    protected License getLicense(Identifier identifier) {
        log.info("Getting license for: " + identifier);
        MavenCentralClient mavenCentralClient = new MavenCentralClient();
        try {
            final Optional<String> repository = mavenCentralClient.getRepository(identifier);
            if (repository.isPresent()) {
                log.info("Getting license from repository: " + repository.get());
                final License license = gitHubClient.getLicense(repository.get());
                if(!Objects.equals(license, License.UNKNOWN)) {
                    return license;
                }
            }
        } catch (Exception e) {
            log.error("Error in getting license by repository", e);
        }
        try {
            final Optional<License> licenceFromPom = mavenCentralClient.getLicenceFromPom(identifier);
            if (licenceFromPom.isPresent()) {
                return licenceFromPom.get();
            }
        } catch (Exception e) {
            log.error("Error in getting license from pom", e);
        }
        log.warn("No license found for: " + identifier);
        return License.UNKNOWN;
    }

    protected GitHubClient getGitHubClient() {
        return gitHubClient;
    }
}
