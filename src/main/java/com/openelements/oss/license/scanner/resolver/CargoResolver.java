package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.Resolver;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CargoResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(CargoResolver.class);

    private final GitHubClient gitHubClient;

    public CargoResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        final String repositoryUrl = getRepositoryUrl(identifier.name());
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

    private Set<Dependency> getAllDependencies(Path pathToProject) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cargo", "tree", "--prefix", "none", "--format", "{p} {r}");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Skip the first line
            String firstLine = reader.readLine();

            final Set<String> allLines = reader.lines().collect(Collectors.toUnmodifiableSet());

            Set<Dependency> dependencies = allLines.stream()
                    .map(line -> {
                        log.info("Mapping dependency for cargo output: '{}'", line);
                        final String[] split = line.split(" ");
                        if(split.length < 2) {
                            throw new IllegalStateException("Invalid line: " + line);
                        }
                        final String name = split[0];
                        final String version = split[1];
                        final String repository;
                        if(split.length > 2 && split[split.length - 1].startsWith("https://github.com/")) {
                            repository = split[split.length - 1];
                        } else if(split.length > 3 && split[split.length - 2].startsWith("https://github.com/")) {
                            repository = split[split.length - 2];
                        } else if(split.length > 4 && split[split.length - 3].startsWith("https://github.com/")) {
                            repository = split[split.length - 3];
                        } else {
                            throw new IllegalStateException("Repository not found in line: " + line);
                        }
                        final License license = gitHubClient.getLicense(repository);
                        return new Dependency(new Identifier(name, version), "unknown", Set.of(), license, repository);
                    }).collect(Collectors.toUnmodifiableSet());

            errorReader.lines().forEach(l -> log.error(l));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                new RuntimeException("Error in calling 'cargo info': " + exitCode);
            }
            return dependencies;
        } catch (Exception e) {
            throw new RuntimeException("Error in getting dependencies" , e);
        }
    }

    private String getRepositoryUrl(String projectName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cargo", "info", projectName);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            final String repositoryUrl = reader.lines().filter(line -> line.startsWith("repository:"))
                    .findFirst()
                    .map(line -> line.substring("repository:".length()).trim())
                    .orElseThrow(() -> new RuntimeException("Repository not found"));

            errorReader.lines().forEach(l -> log.error(l));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                new RuntimeException("Error in calling 'cargo info': " + exitCode);
            }
            return repositoryUrl;
        } catch (Exception e) {
            throw new RuntimeException("Error in calling 'cargo info'", e);
        }
    }
}
