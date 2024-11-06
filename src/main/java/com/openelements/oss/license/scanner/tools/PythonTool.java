package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.api.Identifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonTool {

    private final static Logger log = LoggerFactory.getLogger(PythonTool.class);

    public static Set<Identifier> getDependencies(Path pathToProject) {
        if(!ProcessHelper.checkCommand("python3")) {
            throw new RuntimeException("python3 command is not installed");
        }
        if(!ProcessHelper.checkCommand("source")) {
            throw new RuntimeException("source command is not installed");
        }
        final String installStep;
        if(pathToProject.resolve("requirements.txt").toFile().exists()) {
            log.info("requirements.txt found");
            installStep = "pip install -r requirements.txt";
        } else if(pathToProject.resolve("setup.py").toFile().exists()) {
            log.info("setup.py found");
            installStep = "pip install .";
        } else if(pathToProject.resolve("pyproject.toml").toFile().exists()) {
            log.info("pyproject.toml found");
            installStep = "pip install .";
        }else {
            throw new RuntimeException("No requirements.txt or setup.py found in project");
        }
        try {
            String getDependenciesScript = """
                    #!/bin/bash   
                    python3 -m venv virtualenv
                    source virtualenv/bin/activate 
                    """ + installStep + "\n" + """
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
