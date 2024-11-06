package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.api.Identifier;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CargoTool {

    public record CargoLibrary(Identifier identifier, String repository) {
    }

    public static Set<CargoLibrary> callCargoTree(Path pathToProject) {
        if(!ProcessHelper.checkCommand("cargo")) {
            throw new RuntimeException("cargo command is not installed");
        }
        return ProcessHelper.executeWithResult(l -> extractDependenciesFromCargoTree(l), pathToProject.toFile(), "cargo", "tree", "--prefix", "none", "--format", "{p} {r}");
    }

    private static Set<CargoLibrary> extractDependenciesFromCargoTree(List<String> treeOut) {
        return treeOut.stream().map(line -> {
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
                repository = "UNKNOWN";
            }
            return new CargoLibrary(new Identifier(name, version), repository);
        }).collect(Collectors.toUnmodifiableSet());
    }

    private static String extractRepoFromCargoInfoOut(List<String> cargoInfoOut) {
        return cargoInfoOut.stream()
                .filter(line -> line.startsWith("repository:"))
                .findFirst()
                .map(line -> line.substring("repository:".length()).trim())
                .orElseThrow(() -> new RuntimeException("Repository not found"));
    }

    public static String getRepositoryFromCargoInfo(String projectName) {
        if(!ProcessHelper.checkCommand("cargo")) {
            throw new RuntimeException("cargo command is not installed");
        }
        return ProcessHelper.executeWithResult(l -> extractRepoFromCargoInfoOut(l), "cargo", "info", projectName);
    }
}
