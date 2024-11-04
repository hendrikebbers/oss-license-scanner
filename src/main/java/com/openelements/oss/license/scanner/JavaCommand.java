package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.resolver.PomOnlyResolver;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "java", description = "A tool to scan a library for all dependencies")
public class JavaCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new JavaCommand()).execute("-n", "com.open-elements.hedera:hedera-spring", "-v", "0.9.0");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new PomOnlyResolver(client);
    }

    @Override
    protected String getLanguageType() {
        return "java";
    }
}
