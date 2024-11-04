package com.openelements.oss.license.scanner.api;

import java.nio.file.Path;
import java.util.Set;

public interface Resolver {

    Set<Dependency> resolve(Identifier identifier);

    Set<Dependency> resolve(Path localProjectPath);

}
