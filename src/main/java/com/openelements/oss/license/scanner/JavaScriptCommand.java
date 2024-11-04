package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "js", description = "scan a javascript library for all dependencies")
public class JavaScriptCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new JavaScriptCommand()).execute("-n", "@hashgraph/hedera-local", "-v", "2.31.0");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new NpmResolver(client);
    }
}
