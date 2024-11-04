package com.openelements.oss.license.scanner;
import com.openelements.oss.license.scanner.api.Resolver;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.licenses.KnownLicenses;
import de.siegmar.fastcsv.writer.CsvWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
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

    @Option(names = "-excludeLicenses", split = ",", description = "a comma separated list of licenses that should be excluded from the result")
    private String[] excludedLicences;

    @Override
    public Integer call() {
        try {
            final Identifier identifier = new Identifier(name, version);
            final GitHubClient client = new GitHubClient(token);
            final Resolver resolver = createResolver(client);
            final Set<Dependency> dependencies = resolver.resolve(identifier);
            PrintWriter writer = new PrintWriter(System.out);
            try (CsvWriter csv = CsvWriter.builder().build(writer)) {
                csv.writeRecord("name", "version", "repository", "license", "license-url", "license-source");
                dependencies.stream()
                      //  .filter(d -> KnownLicenses.of(d.license()).map(l -> l.getNames()).filter(names -> excludedLicences == null || excludedLicences.length == 0 || !containsAny(names, excludedLicences)).isPresent())
                        .forEach(d -> csv.writeRecord(d.identifier().name(), d.identifier().version(), d.repository(), d.license().name(), d.license().url(), d.license().source()));
            }
        } catch (Exception e) {
            System.err.println("Error fetching dependencies for " + name + ":" + version);
            e.printStackTrace();
            return ExitCode.SOFTWARE;
        }
        return ExitCode.OK;
    }

    public static boolean containsAny(Set<String> set, String[] array) {
        if (array == null) {
            return false;
        }
        Set<String> arraySet = new HashSet<>(Arrays.asList(array));
        arraySet.retainAll(set);
        return !arraySet.isEmpty();
    }

    protected abstract Resolver createResolver(GitHubClient client);
}
