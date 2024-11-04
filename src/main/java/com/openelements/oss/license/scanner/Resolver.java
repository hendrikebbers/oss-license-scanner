package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.resolver.CargoResolver;
import com.openelements.oss.license.scanner.resolver.GradleResolver;
import com.openelements.oss.license.scanner.resolver.MavenResolver;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import com.openelements.oss.license.scanner.resolver.PomOnlyResolver;
import com.openelements.oss.license.scanner.resolver.SwiftResolver;
import java.util.Set;

public interface Resolver {

    Set<Dependency> resolve(Identifier identifier);

    static Resolver create(ProjectType type) {
        final GitHubClient githubClient = new GitHubClient();
        switch (type) {
            case CARGO:
                return new CargoResolver(githubClient);
            case SWIFT:
                return new SwiftResolver(githubClient);
            case NPM:
                return new NpmResolver(githubClient);
            case MAVEN:
                return new MavenResolver(githubClient);
            case GRADLE:
                return new GradleResolver(githubClient);
            case POM_ONLY:
                return new PomOnlyResolver(githubClient);
            default:
                throw new IllegalArgumentException("Unknown project type: " + type);
        }
    }
}
