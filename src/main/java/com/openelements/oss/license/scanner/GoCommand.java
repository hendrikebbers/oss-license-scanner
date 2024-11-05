package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.resolver.GoResolver;
import com.openelements.oss.license.scanner.resolver.PomResolver;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "go", description = "A tool to scan a library for all dependencies")
public class GoCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new GoCommand()).execute("-r", "https://github.com/hashgraph/hedera-sdk-go", "-v", "v2.49.0");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new GoResolver(client);
    }

    @Override
    protected String getLanguageType() {
        return "go";
    }
}
