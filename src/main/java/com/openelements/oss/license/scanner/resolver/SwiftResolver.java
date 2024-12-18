package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.cache.Cache;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.SwiftTool;
import com.openelements.oss.license.scanner.tools.SwiftTool.SwiftLib;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwiftResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(SwiftResolver.class);

    public SwiftResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        if(Cache.getInstance().containsKeyForSwift(identifier)) {
            return Cache.getInstance().getSwift(identifier);
        }
        final String repositoryUrl = identifier.name();
        final Set<Dependency> dependencies = installLocally(repositoryUrl, identifier.version(), this::resolve);
        Cache.getInstance().putSwift(identifier, dependencies);
        return dependencies;
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        return SwiftTool.callShowDependencies(localProjectPath).stream()
                .map(this::convertToDependency)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Dependency convertToDependency(SwiftLib lib) {
        License license = getLicence(lib.identifier(), lib.repositoryUrl());
        return new Dependency(lib.identifier(), license, lib.repositoryUrl());
    }

    private License getLicence(Identifier identifier, String repository) {
        final Supplier<License> supplier = () -> getLicenseFromProjectUrl(repository)
                .orElse(License.UNKNOWN);
        return LicenseCache.getInstance().computeIfAbsent(identifier, supplier);
    }

}
