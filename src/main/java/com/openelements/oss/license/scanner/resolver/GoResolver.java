package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.cache.Cache;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.GoTool;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(GoResolver.class);

    public GoResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        if (Cache.getInstance().containsKeyForGo(identifier)) {
            return Cache.getInstance().getGo(identifier);
        }
        if (identifier.name().startsWith("github.com/")) {
            final String repositoryUrl = "https://" + identifier.name();
            Set<Dependency> dependencies = installLocally(repositoryUrl, identifier.version(), path -> resolve(path));
            Cache.getInstance().putGo(identifier, dependencies);
            return dependencies;
        } else {
            throw new RuntimeException("Unsupported Go package: " + identifier.name());
        }
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        return GoTool.callModGraph(localProjectPath).stream()
                .map(id -> {
                    final String repository;
                    final License license;
                    if (id.name().startsWith("github.com/")) {
                        repository = "https://" + id.name();
                        license = getLicence(id, repository);
                    } else {
                        log.debug("Unsupported repository: {}", id.name());
                        repository = "unknown";
                        license = LicenseCache.getInstance().computeIfAbsent(id, () -> License.UNKNOWN);
                    }
                    return new Dependency(id, license, repository);
                }).collect(Collectors.toUnmodifiableSet());
    }

    private License getLicence(Identifier identifier, final String repository) {
        final Supplier<License> supplier = () -> getLicenseFromProjectUrl(repository).orElse(License.UNKNOWN);
        return LicenseCache.getInstance()
                .computeIfAbsent(identifier, supplier);
    }
}
