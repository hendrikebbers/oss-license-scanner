package com.openelements.oss.license.scanner;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import de.siegmar.fastcsv.writer.CsvWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;
import java.util.concurrent.Callable;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

public abstract class AbstractCommand implements Callable<Integer> {

    @Option(names = {"-n", "--name"}, description = "the name of the library", required = true)
    private String name;

    @Option(names = {"-v", "--version"}, description = "the version of the library", required = true)
    private String version;

    @Option(names = {"-T", "--token"}, description = "the GitHub token that is used to authenticate with the GitHub API")
    private String token;

    @Override
    public Integer call() {
        try {
            final Identifier identifier = new Identifier(name, version);
            final GitHubClient client = new GitHubClient(token);
            final Resolver resolver = createResolver(client);
            final Set<Dependency> dependencies = resolver.resolve(identifier);
            PrintWriter writer = new PrintWriter(System.out);
            try (CsvWriter csv = CsvWriter.builder().build(writer)) {
                csv.writeRecord("name", "version", "repository", "license", "license-url");
                dependencies.forEach(d -> csv.writeRecord(d.identifier().name(), d.identifier().version(), d.repository(), d.license().name(), d.license().url()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ExitCode.SOFTWARE;
        }
        return ExitCode.OK;
    }

    protected abstract Resolver createResolver(GitHubClient client);
}
