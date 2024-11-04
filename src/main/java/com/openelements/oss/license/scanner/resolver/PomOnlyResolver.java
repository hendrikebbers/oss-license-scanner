package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.MavenCentralClient;
import com.openelements.oss.license.scanner.clients.MavenIdentifier;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.tools.MavenHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomOnlyResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(PomOnlyResolver.class);

    private final GitHubClient gitHubClient;

    public PomOnlyResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        try {
            Path tempDir = Files.createTempDirectory("unzipped_repo");
            try {
                MavenCentralClient mavenCentralClient = new MavenCentralClient();
                final MavenIdentifier mavenIdentifier = new MavenIdentifier(identifier);
                final String pom = mavenCentralClient.getPom(mavenIdentifier);
                Files.write(tempDir.resolve("pom.xml"), pom.getBytes());
                return MavenHelper.getDependenciesFromPom(tempDir).stream()
                        .map(i -> {
                           License license = getLicense(i);
                            return new Dependency(i.toIdentifier(), license, "UNKNOWN");
                        }).collect(Collectors.toSet());
            } catch (Exception e) {
                log.error("Error in resolving dependencies", e);
                return Set.of();
            } finally {
                try (Stream<Path> walk = Files.walk(tempDir)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (IOException e) {
                    log.error("Error in deleting temp directory", e);
                }
            }
        } catch (Exception e) {
            log.error("Error in creating temp directory", e);
        }
        return null;
    }

    protected License getLicense(MavenIdentifier identifier) {
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
}
