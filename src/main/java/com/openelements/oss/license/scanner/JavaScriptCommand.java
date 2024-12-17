package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "js", description = "scan a javascript library for all dependencies")
public class JavaScriptCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new JavaScriptCommand()).execute("-r", "https://github.com/hiero-ledger/hiero-sdk-tck", "-m",
                "manual-additions.csv");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new NpmResolver(client);
    }

    @Override
    protected String getLanguageType() {
        return "js";
    }
}
