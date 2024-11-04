package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import com.openelements.oss.license.scanner.resolver.SwiftResolver;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "js", description = "scan a javascript library for all dependencies")
public class JavaScriptCommand implements Runnable {

    public static void main(String[] args) {
        CommandLine.run(new JavaScriptCommand(), "-n", "@hashgraph/solo", "-v", "0.31.4");
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
        final Resolver resolver = new NpmResolver(client);
        final Set<Dependency> dependencies = resolver.resolve(identifier);
        dependencies.forEach(d -> System.out.println(d.identifier() + " -> " + d.license()));
    }
}
