package com.openelements.oss.license.scanner.resolver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.Resolver;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.tools.SwiftHelper;
import com.openelements.oss.license.scanner.tools.SwiftHelper.SwiftLib;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwiftResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(SwiftResolver.class);

    public SwiftResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        log.info("Resolving dependencies for: {}", identifier);
        final String repositoryUrl = identifier.name();
        return installLocally(repositoryUrl, identifier.version(), this::getAllDependencies);
    }

    private Dependency convertToDependency(SwiftLib lib) {
        License license = gitHubClient.getLicense(lib.repositoryUrl());
        return new Dependency(lib.identifier(), license, lib.repositoryUrl());
    }

    private Set<Dependency> getAllDependencies(Path pathToProject) {
        return SwiftHelper.callShowDependencies(pathToProject).stream()
                .map(this::convertToDependency)
                .collect(Collectors.toUnmodifiableSet());
    }
}
