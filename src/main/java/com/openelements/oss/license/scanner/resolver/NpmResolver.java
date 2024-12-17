package com.openelements.oss.license.scanner.resolver;

import com.google.gson.JsonObject;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.DependencyType;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.cache.Cache;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.tools.NpmTool;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(NpmResolver.class);

    public NpmResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Path localProject) {
        log.info("Analyzing project at {}", localProject);
        final JsonObject jsonObject = NpmTool.callPnpmList(localProject);
        if (!jsonObject.has("dependencies")) {
            return Set.of();
        }
        return jsonObject.get("dependencies").getAsJsonObject().entrySet().stream()
                .map(entry -> {
                    final String name = entry.getKey();
                    final String version = entry.getValue().getAsJsonObject().get("version").getAsString();
                    final Identifier identifier = new Identifier(name, version);
                    final License license = NpmTool.callNpmShowAndReturnLicense(identifier).orElse(License.UNKNOWN);
                    final String repository = NpmTool.callNpmShowAndReturnRepository(identifier).orElse("UNKNOWN");
                    final Dependency dependency = new Dependency(identifier, license, repository,
                            DependencyType.DIRECT);
                    final Set<Dependency> allDependencies = new HashSet<>();
                    allDependencies.add(dependency);

                    String path = entry.getValue().getAsJsonObject().get("path").getAsString();
                    resolve(identifier, path).stream()
                            .map(d -> new Dependency(d.identifier(), d.license(), d.repository(),
                                    DependencyType.TRANSITIVE))
                            .forEach(allDependencies::add);
                    return allDependencies;
                }).flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Set<Dependency> resolve(Identifier identifier, String path) {
        if (Cache.getInstance().containsKeyForJS(identifier)) {
            log.info("Returning cached dependencies for: {}", identifier);
            return Cache.getInstance().getJS(identifier);
        }
        final Set<Dependency> dependencies = resolve(Path.of(path));
        Cache.getInstance().putJS(identifier, dependencies);
        return dependencies;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        if (Cache.getInstance().containsKeyForJS(identifier)) {
            log.info("Returning cached dependencies for: {}", identifier);
            return Cache.getInstance().getJS(identifier);
        }
        final Optional<String> repository = NpmTool.callNpmShowAndReturnRepository(identifier);
        if (repository.isEmpty()) {
            throw new IllegalStateException("No repository found for lib: " + identifier);
        }
        String repositoryUrl = repository.get();
        final Set<Dependency> dependencies = installLocally(repositoryUrl, identifier.version(),
                path -> resolve(identifier, path.toString()));
        Cache.getInstance().putJS(identifier, dependencies);
        return dependencies;
    }
}
