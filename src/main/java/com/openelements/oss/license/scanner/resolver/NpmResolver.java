package com.openelements.oss.license.scanner.resolver;

import com.google.gson.JsonObject;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
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

public class NpmResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(NpmResolver.class);

    private final GitHubClient gitHubClient;

    public NpmResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.debug("Resolving dependencies for: {}", identifier);
        final Dependency asDependency = fromNpmShow(identifier).orElseThrow(() -> new RuntimeException("Dependency not found: " + identifier));
        final String repositoryUrl = asDependency.repository();
        if(repositoryUrl == null) {
            throw new RuntimeException("No repository found for lib: " + identifier);
        }
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

    private Set<Dependency> getDependencies(final JsonObject jsonObject) {
        final Set<Dependency> dependencies = new HashSet<>();
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

    private Set<Dependency> getAllDependencies(Path pathToProject) {
        final JsonObject jsonObject = NpmHelper.callNpmLs(pathToProject);
        return getDependencies(jsonObject);
    }

    private final Map<Identifier, Dependency> cache = new ConcurrentHashMap<>();

    private Optional<Dependency> fromNpmShow(Identifier identifier) {
        if(cache.containsKey(identifier)) {
            return Optional.of(cache.get(identifier));
        }
        final Dependency dependency = NpmHelper.callNpmShowAndReturnRepository(identifier)
                .map(repository -> new Dependency(identifier, gitHubClient.getLicense(repository), repository))
                .orElseGet(() -> new Dependency(identifier, License.UNKNOWN, null));
        cache.put(identifier, dependency);
        return Optional.of(dependency);
    }
}
