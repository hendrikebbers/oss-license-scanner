package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonTool {

    private final static Logger log = LoggerFactory.getLogger(PythonTool.class);

    public static List<String> executeInVirtualEnvironment(Path pathToProject, final String command) {
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
            String fullScriptConent = """
                    #!/bin/bash   
                    python3 -m venv virtualenv
                    source virtualenv/bin/activate 
                    """ + installStep + "\n" + command;
            String uuid = UUID.randomUUID().toString();
            Files.writeString(pathToProject.resolve(uuid+ ".sh"), fullScriptConent, StandardOpenOption.CREATE);
            pathToProject.resolve(uuid +".sh").toFile().setExecutable(true);
            return ProcessHelper.executeWithResult(l -> l, pathToProject.toFile(), "./" + uuid + ".sh");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching python dependencies", e);
        }
    }

    public static Optional<String> getProjectUrlByPipShow(Path pathToProject, Identifier identifier) {
        final String command = "pip show " + identifier.name() + "\n";
        final List<String> output = executeInVirtualEnvironment(pathToProject, command);
        return output.stream().filter(l -> l.startsWith("Home-page:"))
                .map(l -> l.substring("Home-page:".length()).trim())
                .filter(l -> l.contains("github") || l.contains("sourceforge"))
                .map(url -> GitHubClient.normalizeUrl(url))
                .findFirst();
    }

    public static Optional<License>  getLicenseByPipShow(Path pathToProject, Identifier identifier) {
        final String command = "pip show " + identifier.name() + "\n";
        final List<String> output = executeInVirtualEnvironment(pathToProject, command);
        return Optional.empty();
    }

    public static Set<Identifier> getDependencies(Path pathToProject) {
        final String command = """
                    echo "DEPENDENCIES SECTION START"
                    pip freeze
                    echo "DEPENDENCIES SECTION END"
                    """;
        final List<String> output = executeInVirtualEnvironment(pathToProject, command);
        return convert(output);
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
