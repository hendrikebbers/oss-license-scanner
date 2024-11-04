package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.SwiftHelper;
import com.openelements.oss.license.scanner.tools.SwiftHelper.SwiftLib;
import java.nio.file.Path;
import java.util.Set;
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
        final String repositoryUrl = identifier.name();
        return installLocally(repositoryUrl, identifier.version(), this::resolve);
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        return SwiftHelper.callShowDependencies(localProjectPath).stream()
                .map(this::convertToDependency)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Dependency convertToDependency(SwiftLib lib) {
        License license = LicenseCache.getInstance().computeIfAbsent(lib.identifier(), () -> gitHubClient.getLicense(lib.repositoryUrl()));
        return new Dependency(lib.identifier(), license, lib.repositoryUrl());
    }

}
