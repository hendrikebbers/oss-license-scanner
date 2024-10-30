package com.openelements.oss.license.scanner.data;

import java.util.Set;

public record Dependency(Identifier identifier, String scope, Set<Dependency> dependencies, License license, String repository) {

}
