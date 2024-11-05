package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.MavenCentralClient;
import com.openelements.oss.license.scanner.clients.MavenIdentifier;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.MavenTool;
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

public class PomResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(PomResolver.class);

    public PomResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        MavenCentralClient mavenCentralClient = new MavenCentralClient();
        return MavenTool.callListDependencies(localProjectPath).stream()
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
        final License cachedLicense = LicenseCache.getInstance().getLicense(identifier.toIdentifier());
        if(cachedLicense != null) {
            return cachedLicense;
        }
        MavenCentralClient mavenCentralClient = new MavenCentralClient();
        try {
            final Optional<String> repository = mavenCentralClient.getRepository(identifier);
            if (repository.isPresent()) {
                log.info("Getting license from repository: " + repository.get());
                final License licenseByRepo = gitHubClient.getLicense(repository.get()).orElse(License.UNKNOWN);
                if(!Objects.equals(licenseByRepo, License.UNKNOWN)) {
                    LicenseCache.getInstance().addLicense(identifier.toIdentifier(), licenseByRepo);
                    return licenseByRepo;
                }
            }
        } catch (Exception e) {
            log.error("Error in getting license by repository", e);
        }
        try {
            log.info("Getting license from pom");
            final License licenceFromPom = mavenCentralClient.getLicenceFromPom(identifier).orElse(License.UNKNOWN);
            if(!Objects.equals(licenceFromPom, License.UNKNOWN)) {
                LicenseCache.getInstance().addLicense(identifier.toIdentifier(), licenceFromPom);
                return licenceFromPom;
            }
        } catch (Exception e) {
            log.error("Error in getting license from pom", e);
        }
        log.warn("No license found for: " + identifier);
        LicenseCache.getInstance().addLicense(identifier.toIdentifier(), License.UNKNOWN);
        return License.UNKNOWN;
    }
}
