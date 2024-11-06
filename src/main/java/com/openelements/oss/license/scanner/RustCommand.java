package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.resolver.CargoResolver;
import java.util.Set;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "rust", description = "scan a rust library for all dependencies")
public class RustCommand extends AbstractCommand {

    public static void main(String[] args) {
        new CommandLine(new RustCommand()).execute("-r", "https://github.com/hyperledger/aries-askar");
    }

    @Override
    protected Resolver createResolver(GitHubClient client) {
        return new CargoResolver(client);
    }

    @Override
    protected String getLanguageType() {
        return "rust";
    }
}
