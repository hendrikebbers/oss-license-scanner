package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.tools.CargoHelper;
import com.openelements.oss.license.scanner.tools.CargoHelper.CargoLibrary;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CargoResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(CargoResolver.class);

    public CargoResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        final String repositoryUrl = CargoHelper.getRepositoryFromCargoInfo(identifier.name());
        return installLocally(repositoryUrl, identifier.version(), pathToProject -> {
            final Set<CargoLibrary> libs = CargoHelper.callCargoTree(pathToProject);
            return convertToDependency(libs);
        });
    }

    private Set<Dependency> convertToDependency(Set<CargoLibrary> libs) {
        return libs.stream().map(lib -> {
            final License license = gitHubClient.getLicense(lib.repository());
            return new Dependency(lib.identifier(), "unknown", Set.of(), license, lib.repository());
        }).collect(Collectors.toUnmodifiableSet());
    }

}
