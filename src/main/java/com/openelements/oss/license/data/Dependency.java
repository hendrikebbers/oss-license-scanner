package com.openelements.oss.license.data;

import java.util.Set;

public record Dependency(Identifier identifier, String scope, Set<Dependency> dependencies, License license, String repository) {

}
