package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.resolver.SwiftResolver;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "swift", description = "scan a swift library for all dependencies")
public class SwiftCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new SwiftCommand()).execute("-n", "https://github.com/hashgraph/hedera-sdk-swift", "-v", "0.32.0");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new SwiftResolver(client);
    }
}
