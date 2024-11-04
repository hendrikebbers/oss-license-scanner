package com.openelements.oss.license.scanner;

import com.openelements.oss.license.scanner.clients.GitHubClient;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.resolver.CargoResolver;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import com.openelements.oss.license.scanner.resolver.PomOnlyResolver;
import com.openelements.oss.license.scanner.resolver.SwiftResolver;
import java.util.Set;

public interface Resolver {

    Set<Dependency> resolve(Identifier identifier);
}
