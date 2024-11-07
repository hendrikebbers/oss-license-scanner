package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.clients.CratesClient;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.CargoTool;
import com.openelements.oss.license.scanner.tools.CargoTool.CargoLibrary;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
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
        final String repositoryUrl = CargoTool.getRepositoryFromCargoInfo(identifier.name());
        return installLocally(repositoryUrl, identifier.version(), path -> resolve(path));
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        final Set<CargoLibrary> libs = CargoTool.callCargoTree(localProjectPath);
        return libs.stream().map(lib -> {
            final License license = getLicence(lib);
            return new Dependency(lib.identifier(), license, lib.repository());
        }).collect(Collectors.toUnmodifiableSet());
    }

    private License getLicence(CargoLibrary library) {
        final Supplier<License> supplier = () -> CratesClient.getLicenceForCrate(library.identifier())
                .or(() -> getLicenseFromProjectUrl(library.repository()))
                .orElse(License.UNKNOWN);
        return LicenseCache.getInstance()
                .computeIfAbsent(library.identifier(), supplier);
    }

}
