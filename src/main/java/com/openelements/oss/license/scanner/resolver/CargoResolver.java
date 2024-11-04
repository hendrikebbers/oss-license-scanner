package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.tools.CargoHelper;
import com.openelements.oss.license.scanner.tools.CargoHelper.CargoLibrary;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CargoResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(CargoResolver.class);

    private final GitHubClient gitHubClient;

    public CargoResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        final String repositoryUrl = getRepositoryUrl(identifier.name());
        final String tag = gitHubClient.findMatchingTag(repositoryUrl, identifier.version())
                .orElseThrow(() -> new RuntimeException("No tag found for version: " + identifier.version()));
        final Path pathToProject = gitHubClient.download(repositoryUrl, tag);
        try {
            return getAllDependencies(pathToProject);
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

    private Set<Dependency> convertToDependency(Set<CargoLibrary> libs) {
        return libs.stream().map(lib -> {
            final License license = gitHubClient.getLicense(lib.repository());
            return new Dependency(lib.identifier(), "unknown", Set.of(), license, lib.repository());
        }).collect(Collectors.toUnmodifiableSet());
    }

    private Set<Dependency> getAllDependencies(Path pathToProject) {
        final Set<CargoLibrary> libs = CargoHelper.callCargoTree(pathToProject);
        return convertToDependency(libs);
    }

    private String getRepositoryUrl(String projectName) {
        return CargoHelper.getRepositoryFromCargoInfo(projectName);
    }
}
