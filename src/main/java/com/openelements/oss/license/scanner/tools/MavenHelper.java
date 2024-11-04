package com.openelements.oss.license.scanner.tools;

import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.resolver.MavenResolver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenHelper {

    private final static Logger log = LoggerFactory.getLogger(MavenHelper.class);

    private static void installMavenWrapper(Path pathToProject) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mvn", "wrapper:wrapper");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            reader.lines().forEach(l -> log.debug(l));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Process ended with bad status: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error in installing maven wrapper", e);
        }
    }

    public static Set<Identifier> getDependenciesFromPom(Path pathToProject) {
        if(!Paths.get(pathToProject.toFile().getAbsolutePath(), "mvnw").toFile().exists()) {
            installMavenWrapper(pathToProject);
        }

        if(!Paths.get(pathToProject.toFile().getAbsolutePath(), "mvnw").toFile().canExecute()) {
            Paths.get(pathToProject.toFile().getAbsolutePath(), "mvnw").toFile().setExecutable(true);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("./mvnw", "dependency:list");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                final List<String> allLines = reader.lines().collect(Collectors.toList());
                allLines.forEach(l -> log.info(l));
                return allLines.stream()
                        .filter(l -> l.startsWith("[INFO]    "))
                        .map(l -> l.substring("[INFO]    ".length()))
                        .filter(l -> !Objects.equals(l, "none"))
                        .map(l -> {
                            final String[] split = l.split(":");
                            if (split.length < 3) {
                                throw new RuntimeException("Invalid dependency: " + l);
                            }
                            return new Identifier(split[0] + ":" + split[1], split[3]);
                        })
                        .collect(Collectors.toUnmodifiableSet());
            } finally {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    errorReader.lines().forEach(l -> log.error(l));
                    new RuntimeException("Process ended with bad status: " + exitCode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in getting dependencies from maven", e);
        }
    }
}
