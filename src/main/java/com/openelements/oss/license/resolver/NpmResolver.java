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
import java.util.stream.Collectors;
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
        log.info("Resolving dependencies for: {}", identifier);
        final Dependency asDependency = fromNpmShow(identifier);
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


      //  https://www.npmjs.com/package/@hashgraph/sdk/v/2.52.0

      //  npm show @hashgraph/sdk@2.52 --json
    }

    private Set<Dependency> getAllDependencies(Path pathToProject) {
        executeNpmInstall(pathToProject);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("npm", "ls", "--all", "--json");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            Set<Dependency> dependencies = new HashSet<>();
            final JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            System.out.println(jsonObject);

            jsonObject.get("dependencies").getAsJsonObject().entrySet().stream()
                    .map(e -> {
                        final String name = e.getKey();
                        final String version = e.getValue().getAsJsonObject().get("version").getAsString();
                        return new Identifier(name, version);
                    })
                    .map(this::fromNpmShow)
                    .forEach(dependencies::add);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Error in calling 'npm': " + exitCode);
            }
            return Collections.unmodifiableSet(dependencies);
        } catch (Exception e) {
            log.error("Error in executing npm install", e);
        }
        return null;
    }

    private Dependency fromNpmShow(Identifier identifier) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("npm", "show", identifier.name() + "@" + identifier.version(), "--json");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            final JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            String scope = "unknown";
            Set<Dependency> dependencies = Set.of();
            String repository = jsonObject.get("repository").getAsJsonObject().get("url").getAsString();
            License license = gitHubClient.getLicense(repository);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Error in calling 'npm': " + exitCode);
            }
            return new Dependency(identifier, scope, dependencies, license, repository);
        } catch (Exception e) {
            log.error("Error in executing npm install", e);
        }
        return null;
    }



    public void executeNpmInstall(Path pathToProject) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("npm", "install");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            reader.lines().forEach(l -> log.info(l));
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
