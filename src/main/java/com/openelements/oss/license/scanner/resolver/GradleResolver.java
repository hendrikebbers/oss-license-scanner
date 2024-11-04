package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.MavenCentralClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradleResolver extends AbstractJavaResolver {

    private final static Logger log = LoggerFactory.getLogger(GradleResolver.class);

    public GradleResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    public Set<Dependency> getDependenciesFromProject(Path pathToProject) {
        Set<Dependency> dependencies = new HashSet<>();

        if(!Paths.get(pathToProject.toFile().getAbsolutePath(), "gradlew").toFile().canExecute()) {
            Paths.get(pathToProject.toFile().getAbsolutePath(), "gradlew").toFile().setExecutable(true);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("./gradlew", "dependencies");
            processBuilder.directory(pathToProject.toFile());
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            reader.lines()
                    //.filter(l -> l.startsWith("+---") || l.startsWith("|    ") || l.startsWith("\\---"))
                    .forEach(l -> System.out.println(l));
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Process ended with bad status: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error in executing gradle", e);
        }
        return Collections.unmodifiableSet(dependencies);
    }
}
