package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.CargoHelper;
import com.openelements.oss.license.scanner.tools.CargoHelper.CargoLibrary;
import java.nio.file.Path;
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
        return installLocally(repositoryUrl, identifier.version(), path -> resolve(path));
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        final Set<CargoLibrary> libs = CargoHelper.callCargoTree(localProjectPath);
        return libs.stream().map(lib -> {
            final License license = LicenseCache.getInstance().computeIfAbsent(lib.identifier(), () -> gitHubClient.getLicense(lib.repository()));
            return new Dependency(lib.identifier(), license, lib.repository());
        }).collect(Collectors.toUnmodifiableSet());
    }

}
