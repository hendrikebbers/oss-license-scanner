package com.openelements.oss.license.scanner.resolver;

import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.clients.CratesClient;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.clients.PypiClient;
import com.openelements.oss.license.scanner.licenses.LicenseCache;
import com.openelements.oss.license.scanner.tools.CargoTool.CargoLibrary;
import com.openelements.oss.license.scanner.tools.GoTool;
import com.openelements.oss.license.scanner.tools.ProcessHelper;
import com.openelements.oss.license.scanner.tools.PythonTool;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonResolver extends AbstractResolver {

    private final static Logger log = LoggerFactory.getLogger(PythonResolver.class);

    public PythonResolver(GitHubClient gitHubClient) {
        super(gitHubClient);
    }

    @Override
    public Set<Dependency> resolve(Identifier identifier) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Dependency> resolve(Path localProjectPath) {
        final Set<Identifier> dependencies = PythonTool.getDependencies(localProjectPath);
        return dependencies.stream()
                .map(i -> {
                    final String repository = PypiClient.getRepository(i)
                            .orElse("Unknown");
                    final License license = getLicence(i, repository);
                    return new Dependency(i, license, repository);
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    private License getLicence(Identifier identifier, String repository) {
        return PypiClient.getLicense(identifier)
                .orElseGet(() -> getLicenseFromGitHub(repository));
    }
}
