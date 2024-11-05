package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.clients.MavenIdentifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenTool {

    private final static Logger log = LoggerFactory.getLogger(MavenTool.class);

    private static void installMavenWrapper(Path pathToProject) {
        if(!ProcessHelper.checkCommand("mvn")) {
            throw new RuntimeException("mvn command is not installed");
        }
        ProcessHelper.execute(l -> l.forEach(log::info), pathToProject.toFile(), "mvn", "wrapper:wrapper");
    }

    private static Set<MavenIdentifier> extractDependenciesFromMavenOut(List<String> lines) {
        lines.forEach(l -> log.info(l));
        return lines.stream()
                .filter(l -> l.startsWith("[INFO]    "))
                .map(l -> l.substring("[INFO]    ".length()))
                .filter(l -> !Objects.equals(l, "none"))
                .map(l -> {
                    final String[] split = l.split(":");
                    if (split.length < 3) {
                        throw new RuntimeException("Invalid dependency: " + l);
                    }
                    return new MavenIdentifier(split[0], split[1], split[3]);
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    public static Set<MavenIdentifier> callListDependencies(Path pathToProject) {
        if(!Paths.get(pathToProject.toFile().getAbsolutePath(), "mvnw").toFile().exists()) {
            installMavenWrapper(pathToProject);
        }
        if(!Paths.get(pathToProject.toFile().getAbsolutePath(), "mvnw").toFile().canExecute()) {
            Paths.get(pathToProject.toFile().getAbsolutePath(), "mvnw").toFile().setExecutable(true);
        }
        return ProcessHelper.executeWithResult(l -> extractDependenciesFromMavenOut(l), pathToProject.toFile(), "./mvnw", "dependency:list");
    }
}
