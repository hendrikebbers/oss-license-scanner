package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.resolver.CargoResolver;
import com.openelements.oss.license.scanner.resolver.SwiftResolver;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "swift", description = "scan a swift library for all dependencies")
public class SwiftCommand implements Runnable {

    public static void main(String[] args) {
        CommandLine.run(new SwiftCommand(), "-n", "https://github.com/hashgraph/hedera-sdk-swift", "-v", "0.32.0");
    }

    @Option(names = {"-v", "--version"}, description = "the version of the library", required = true)
    private String version;

    @Option(names = {"-n", "--name"}, description = "the name of the library", required = true)
    private String name;

    @Option(names = {"-t", "--token"}, description = "the GitHub token that is used to authenticate with the GitHub API")
    private String token;

    @Override
    public void run() {
        final Identifier identifier = new Identifier(name, version);
        final GitHubClient client = new GitHubClient(token);
        final Resolver resolver = new SwiftResolver(client);
        final Set<Dependency> dependencies = resolver.resolve(identifier);
        dependencies.forEach(d -> System.out.println(d.identifier() + " -> " + d.license()));
    }
}
