package com.openelements.oss.license.scanner.api;

import java.util.Set;

public interface Resolver {

    Set<Dependency> resolve(Identifier identifier);
}
