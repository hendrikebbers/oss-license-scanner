package com.openelements.oss.license.scanner.resolver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
        executeNpmInstall(pathToProject);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("npm", "ls", "--production", "--json");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            Set<Dependency> dependencies = new HashSet<>();
            final JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            dependencies.addAll(getDependencies(jsonObject));

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Error in calling 'npm': " + exitCode);
            }
            return Collections.unmodifiableSet(dependencies);
        } catch (Exception e) {
            log.error("Error in executing npm install", e);
        }
        return Set.of();
    }

    private final Map<Identifier, Dependency> cache = new ConcurrentHashMap<>();

    private Optional<Dependency> fromNpmShow(Identifier identifier) {
        if(cache.containsKey(identifier)) {
            return Optional.of(cache.get(identifier));
        }
        try {
            log.debug("Executing npm show for: {}", identifier);
            ProcessBuilder processBuilder = new ProcessBuilder("npm", "show", identifier.name() + "@" + identifier.version(), "--json");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            final JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String scope = "unknown";
            Set<Dependency> dependencies = Set.of();
            String repository;
            if(!jsonObject.has("repository")) {
                log.error("No repository found for: {}", identifier);
                repository = "UNKNOWN";
            }else if(!jsonObject.get("repository").isJsonObject()) {
                log.error("No repository found for: {}", identifier);
                repository = "UNKNOWN";
            } else if(!jsonObject.get("repository").getAsJsonObject().has("url")) {
                log.error("No repository url found for: {}", identifier);
                repository = "UNKNOWN";
            } else {
                repository = jsonObject.get("repository").getAsJsonObject().get("url").getAsString();
            }
            License license = gitHubClient.getLicense(repository);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Error in calling 'npm': " + exitCode);
            }
            Dependency dependency = new Dependency(identifier, scope, dependencies, license, repository);
            cache.put(identifier, dependency);
            return Optional.of(dependency);
        } catch (Exception e) {
            log.error("Error in executing npm show for " + identifier, e);
        }
        return Optional.empty();
    }

    public void executeNpmInstall(Path pathToProject) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("npm", "install");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            reader.lines().forEach(l -> log.debug(l));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Error in calling 'swift': " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error in executing npm install", e);
        }
    }
}
