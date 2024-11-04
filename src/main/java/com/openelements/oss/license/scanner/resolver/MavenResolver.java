package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.clients.MavenCentralClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.tools.MavenHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenResolver extends AbstractJavaResolver {

    private final static Logger log = LoggerFactory.getLogger(MavenResolver.class);

    public MavenResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    protected Set<Dependency> getDependenciesFromProject(Path pathToProject) {
        return MavenHelper.getDependenciesFromPom(pathToProject).stream()
                .map(identifier -> {
                    final License license = getLicense(identifier);
                    return new Dependency(identifier, "UNLNOWN", Set.of(), license, "UNKNOWN");
                })
                .collect(Collectors.toUnmodifiableSet());
    }





}
