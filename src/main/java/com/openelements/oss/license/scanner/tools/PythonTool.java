package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.api.Identifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PythonTool {

    public static Set<Identifier> getDependencies(Path pathToProject) {
        if(!ProcessHelper.checkCommand("python3")) {
            throw new RuntimeException("python3 command is not installed");
        }
        if(!ProcessHelper.checkCommand("source")) {
            throw new RuntimeException("source command is not installed");
        }
        if(!pathToProject.resolve("requirements.txt").toFile().exists()) {
            throw new IllegalArgumentException("requirements.txt file not found");
        }
        try {
            String getDependenciesScript = """
                    #!/bin/bash   
                    python3 -m venv virtualenv
                    source virtualenv/bin/activate 
                    pip install -r requirements.txt
                    echo "DEPENDENCIES SECTION START"
                    pip freeze
                    echo "DEPENDENCIES SECTION END"
                    """;
            Files.writeString(pathToProject.resolve("listDependencies.sh"), getDependenciesScript, StandardOpenOption.CREATE);
            pathToProject.resolve("listDependencies.sh").toFile().setExecutable(true);
            return ProcessHelper.executeWithResult(l -> convert(l), pathToProject.toFile(), "./listDependencies.sh");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching python dependencies", e);
        }
    }

    private static Set<Identifier> convert(List<String> lines) {
        final int start = lines.indexOf("DEPENDENCIES SECTION START");
        final int end = lines.indexOf("DEPENDENCIES SECTION END");
        final List<String> dependencies = lines.subList(start + 1, end);
        return dependencies.stream()
                .map(l -> l.split("=="))
                .map(l -> {
                    if (l.length < 2) {
                        throw new RuntimeException("Invalid dependency: " + l);
                    }
                    return new Identifier(l[0], l[1]);
                })
                .collect(Collectors.toUnmodifiableSet());
    }
 }
