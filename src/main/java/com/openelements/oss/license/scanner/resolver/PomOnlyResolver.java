package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.MavenCentralClient;
import com.openelements.oss.license.scanner.clients.MavenIdentifier;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.MavenHelper;
import java.io.File;
import java.io.IOException;
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

public class PomOnlyResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(PomOnlyResolver.class);

    public PomOnlyResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        MavenCentralClient mavenCentralClient = new MavenCentralClient();
        return MavenHelper.callListDependencies(localProjectPath).stream()
                .map(i -> {
                    final License license = getLicense(i);
                    final String repository = mavenCentralClient.getRepository(i)
                            .map(r -> GitHubClient.normalizeUrl(r))
                            .orElse("UNKNOWN");
                    return new Dependency(i.toIdentifier(), license, repository);
                }).collect(Collectors.toSet());
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
                //TODO: throw exception if type of pom == pom/bom...
                Files.write(tempDir.resolve("pom.xml"), pom.getBytes());
                return resolve(tempDir);
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
            throw new RuntimeException("Error in creating temp directory", e);
        }
    }

    protected License getLicense(MavenIdentifier identifier) {
        log.info("Getting license for: " + identifier);
        MavenCentralClient mavenCentralClient = new MavenCentralClient();
        try {
            final Optional<String> repository = mavenCentralClient.getRepository(identifier);
            if (repository.isPresent()) {
                log.info("Getting license from repository: " + repository.get());
                final License license = LicenseCache.getInstance().computeIfAbsent(identifier.toIdentifier(), () ->gitHubClient.getLicense(repository.get()));
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
