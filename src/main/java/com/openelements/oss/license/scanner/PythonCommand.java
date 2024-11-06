package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.resolver.GoResolver;
import com.openelements.oss.license.scanner.resolver.PythonResolver;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "python", description = "A tool to scan a library for all dependencies")
public class PythonCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new PythonCommand()).execute("-r", "https://github.com/hyperledger/indy-node");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new PythonResolver(client);
    }

    @Override
    protected String getLanguageType() {
        return "python";
    }
}
