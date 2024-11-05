package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.api.Identifier;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GoTool {

    public static Set<Identifier> callModGraph(Path pathToProject) {
        if(!ProcessHelper.checkCommand("go")) {
            throw new RuntimeException("go command is not installed");
        }
        return ProcessHelper.executeWithResult(l -> getDependenciesFromModGraph(l), pathToProject.toFile(), "go", "mod", "graph");
    }

    private static Set<Identifier> getDependenciesFromModGraph(List<String> output) {
        return output.stream().map(l -> {
            final String[] split = l.split(" ");
            if (split.length < 2) {
                throw new RuntimeException("Invalid dependency definition: " + l);
            }
            final String dependencyString = split[1];
            final String[] dependencySplit = dependencyString.split("@");
            if (dependencySplit.length < 2) {
                throw new RuntimeException("Invalid dependency definition: " + l);
            }
            return new Identifier(dependencySplit[0], dependencySplit[1]);
        }).collect(Collectors.toUnmodifiableSet());
    }
}
