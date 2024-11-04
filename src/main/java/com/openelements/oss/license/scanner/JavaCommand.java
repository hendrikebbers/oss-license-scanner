package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import com.openelements.oss.license.scanner.resolver.PomOnlyResolver;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

@Command(name = "java", description = "A tool to scan a library for all dependencies")
public class JavaCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new JavaCommand()).execute("-n", "com.hedera.hashgraph:app", "-v", "0.55.2");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new PomOnlyResolver(client);
    }
}
