package com.openelements.oss.license.scanner.resolver;

import com.google.gson.JsonObject;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.NpmHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(NpmResolver.class);

    public NpmResolver(GitHubClient gitHubClient) {
       super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Path localProject) {
        final JsonObject jsonObject = NpmHelper.callNpmLs(localProject);
        return getDependencies(jsonObject);
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.debug("Resolving dependencies for: {}", identifier);
        final Dependency asDependency = fromNpmShow(identifier).orElseThrow(() -> new RuntimeException("Dependency not found: " + identifier));
        final String repositoryUrl = asDependency.repository();
        if(repositoryUrl == null) {
            throw new RuntimeException("No repository found for lib: " + identifier);
        }
        return installLocally(repositoryUrl, identifier.version(), path -> resolve(path));
    }

    private Set<Dependency> getDependencies(final JsonObject jsonObject) {
        final Set<Dependency> dependencies = new HashSet<>();
        if(!jsonObject.has("dependencies")) {
            return dependencies;
        }
        jsonObject.get("dependencies").getAsJsonObject().entrySet().stream().parallel()
                .map(e -> {
                    final String name = e.getKey();
                    if (!e.getValue().getAsJsonObject().has("version")) {
                        log.error("No version found for dependency: {}", name);
                        return new Identifier(name, "UNKNOWN");
                    }
                    final String version = e.getValue().getAsJsonObject().get("version").getAsString();
                    return new Identifier(name, version);
                })
                .map(identifier -> {
                    final Optional<Dependency> dependency = fromNpmShow(identifier);
                    if(dependency.isEmpty()) {
                        log.error("Dependency not found: {}", identifier);
                    }
                    return dependency;
                }).forEach(o -> o.ifPresent(dependencies::add));

        jsonObject.get("dependencies").getAsJsonObject().entrySet().stream().parallel()
                .forEach(e -> {
                    if (e.getValue().getAsJsonObject().has("dependencies")) {
                        Set<Dependency> transitiveDependencies = getDependencies(e.getValue().getAsJsonObject());
                        dependencies.addAll(transitiveDependencies);
                    }
                });
        return dependencies;
    }

    private final Map<Identifier, Dependency> cache = new ConcurrentHashMap<>();

    private Optional<Dependency> fromNpmShow(Identifier identifier) {
        if(cache.containsKey(identifier)) {
            return Optional.of(cache.get(identifier));
        }
        final Dependency dependency = NpmHelper.callNpmShowAndReturnRepository(identifier)
                .map(repository -> new Dependency(identifier, LicenseCache.getInstance().computeIfAbsent(identifier, () -> gitHubClient.getLicense(repository)), repository))
                .orElseGet(() -> new Dependency(identifier, License.UNKNOWN, null));
        cache.put(identifier, dependency);
        return Optional.of(dependency);
    }
}
