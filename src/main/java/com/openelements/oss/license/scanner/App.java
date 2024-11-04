package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.resolver.CargoResolver;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class App {

    public static void main(String[] args) {

        //final Set<Dependency> dependencies = forHederaRustSdk();
        final Set<Dependency> dependencies = forHederaSwiftSdk();
        //final Set<Dependency> dependencies = forHederaJsSdk();
        //final Set<Dependency> dependencies = forHederaEnterprise();
        //final Set<Dependency> dependencies = forJavaSdk();
        print(dependencies);
    }

    private static Set<Dependency> forHederaRustSdk() {
        final Resolver resolver = Resolver.create(ProjectType.CARGO);
        return resolver.resolve(new Identifier("hedera", "0.29.0"));
    }

    private static Set<Dependency> forHederaSwiftSdk() {
        final Resolver resolver = Resolver.create(ProjectType.SWIFT);
        return resolver.resolve(new Identifier("https://github.com/hashgraph/hedera-sdk-swift", "0.32.0"));
    }

    private static Set<Dependency> forHederaJsSdk() {
        final Resolver resolver = Resolver.create(ProjectType.NPM);
        return resolver.resolve(new Identifier("@hashgraph/solo", "0.31.4"));
    }

    private static Set<Dependency> forHederaEnterprise() {
        final Resolver resolver = Resolver.create(ProjectType.POM);
        return resolver.resolve(new Identifier("https://github.com/OpenElements/hedera-enterprise", "0.9.0"));
    }

    private static Set<Dependency> forJavaSdk() {
        final Resolver resolver = Resolver.create(ProjectType.POM);
        return resolver.resolve(new Identifier("com.hedera.hashgraph:app", "0.55.2"));
    }
    private static void print(Set<Dependency> dependencies) {
        dependencies.stream()
                .filter(d -> !d.license().isMit())
                .filter(d -> !d.license().isApache())
                .filter(d -> !d.license().isBsd3())
                .filter(d -> !d.license().isEpl2())
                .filter(d -> !d.license().isEpl1())
                .filter(d -> !d.license().isBsd2())
                .forEach(d -> System.out.println(d.identifier() + " -> " + d.license()));
    }
}
