package com.openelements.oss.license.resolver;

import com.openelements.oss.license.Resolver;
import com.openelements.oss.license.clients.MavenCentralClient;
import com.openelements.oss.license.data.Dependency;
import com.openelements.oss.license.data.Identifier;
import com.openelements.oss.license.data.License;
import com.openelements.oss.license.clients.GitHubClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolver implements Resolver {

    private final static Logger log = LoggerFactory.getLogger(MavenResolver.class);

    private final GitHubClient gitHubClient;

    public MavenResolver(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        //TODO: extract later
        final String repositoryUrl = identifier.name();
        final String tag = gitHubClient.findMatchingTag(repositoryUrl, identifier.version())
                .orElseThrow(() -> new RuntimeException("No tag found for version: " + identifier.version()));
        final Path pathToProject = gitHubClient.download(repositoryUrl, tag);
        return getDependencies(pathToProject);
    }

    public Set<Dependency> getDependencies(Path pathToProject) {
        Set<Dependency> dependencies = new HashSet<>();

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
            reader.lines()
                    .filter(l -> l.startsWith("[INFO]    "))
                    .map(l -> l.substring("[INFO]    ".length()))
                    .filter(l -> !Objects.equals(l, "none"))
                    .map(l -> {
                        final String[] split = l.split(":");
                        if(split.length < 3) {
                            throw new RuntimeException("Invalid dependency: " + l);
                        }
                        Identifier identifier = new Identifier(split[0] + ":" + split[1], split[3]);
                        String scope = "";
                        if(split.length > 4) {
                            scope = split[4];
                        }
                        License license = getLicense(identifier);
                        return new Dependency(identifier, scope, Set.of(), license, null);
                    })
                    .forEach(dependencies::add);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                errorReader.lines().forEach(l -> log.error(l));
                new RuntimeException("Process ended with bad status: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error in executing mvn", e);
        }
        return Collections.unmodifiableSet(dependencies);
    }

    private License getLicense(Identifier identifier) {
        //https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.0/javax.servlet-api-4.0.0.pom

        MavenCentralClient mavenCentralClient = new MavenCentralClient();
        try {
            final Optional<String> repository = mavenCentralClient.getRepository(identifier);
            if (repository.isPresent()) {
                return gitHubClient.getLicense(repository.get());
            }
        } catch (Exception e) {
            log.error("Error in getting license by repository", e);
        }
        try {
            final Optional<License> licenceFromPom = mavenCentralClient.getLicenceFromPom(identifier);
            if (licenceFromPom.isPresent()) {
                return licenceFromPom.get();
            }
        } catch (Exception e) {
            log.error("Error in getting license from pom", e);
        }
        return License.UNKNOWN;
    }

    public void installMavenWrapper(Path pathToProject) {
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
}
