package com.openelements.oss.license;

import com.openelements.oss.license.data.Dependency;
import com.openelements.oss.license.data.Identifier;
import com.openelements.oss.license.clients.GitHubClient;
import com.openelements.oss.license.resolver.MavenResolver;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class App {

    public static void main(String[] args) {
        final GitHubClient githubClient = new GitHubClient();

        //final Resolver resolver = new CargoResolver(githubClient);
        //final Set<Dependency> dependencies = resolver.resolve(new Identifier("hedera", "0.29.0"));

       // final Resolver resolver = new SwiftResolver(githubClient);
       // final Set<Dependency> dependencies = resolver.resolve(new Identifier("https://github.com/hashgraph/hedera-sdk-swift", "0.32.0"));

       // final Resolver resolver = new NpmResolver(githubClient);
       // final Set<Dependency> dependencies = resolver.resolve(new Identifier("@hashgraph/sdk", "2.51.0"));

        final Resolver resolver = new MavenResolver(githubClient);
        final Set<Dependency> dependencies = resolver.resolve(new Identifier("https://github.com/OpenElements/hedera-enterprise", "0.9.0"));


        final Set<Dependency> flatDependencies = flattenDependencies(dependencies);
        flatDependencies.stream()
                .filter(d -> !d.license().isMit())
                .filter(d -> !d.license().isApache())
                .filter(d -> !d.license().isBsd3())
                .filter(d -> !d.license().isEpl2())
                .filter(d -> !d.license().isBsd2())
                .forEach(d -> System.out.println(d.identifier() + " -> " + d.license()));
    }

    public static Set<Dependency> flattenDependencies(Collection<Dependency> dependencies) {
        Set<Dependency> flatSet = new HashSet<>();
        dependencies.forEach(d -> {
            flatSet.add(d);
                flatSet.addAll(flattenDependencies(d.dependencies()));
        });
        return flatSet;
    }
}
