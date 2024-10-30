package com.openelements.oss.license.resolver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.Resolver;
import com.openelements.oss.license.data.Dependency;
import com.openelements.oss.license.data.Identifier;
import com.openelements.oss.license.data.License;
import com.openelements.oss.license.git.GitHubClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwiftResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(SwiftResolver.class);

    private final GitHubClient gitHubClient;

    public SwiftResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        final String repositoryUrl = identifier.name();
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
        //swift package show-dependencies --format json
    }

    private Dependency convertToDependency(JsonObject identity) {
        final Set<Dependency> dependencies = new HashSet<>();
        final Identifier identifier = new Identifier(identity.get("name").getAsString(), identity.get("version").getAsString());
        final String scope = "UNKNOWN";
        identity.get("dependencies").getAsJsonArray().forEach(d -> {
            dependencies.add(convertToDependency(d.getAsJsonObject()));
        });
        final String githubUrl = identity.get("url").getAsString();
        License license = gitHubClient.getLicense(githubUrl);
        return new Dependency(identifier, scope, dependencies, license, githubUrl);
    }

    private Set<Dependency> getAllDependencies(Path pathToProject) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("swift", "package", "show-dependencies", "--format",
                    "json");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            final Set<Dependency> dependencies = new HashSet<>();
            JsonParser.parseReader(reader).getAsJsonObject().get("dependencies").getAsJsonArray().forEach(d -> {
                dependencies.add(convertToDependency(d.getAsJsonObject()));
            });
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Error in calling 'swift': " + exitCode);
            }
            return dependencies;
        } catch (Exception e) {
            log.error("Error in getting dependencies", e);
        }
        return Set.of();
    }
}
