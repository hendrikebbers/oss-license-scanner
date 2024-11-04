package com.openelements.oss.license.scanner.api;

public record Dependency(Identifier identifier, License license, String repository) implements Comparable<Dependency> {

    @Override
    public int compareTo(Dependency o) {
        return identifier.compareTo(o.identifier);
    }

    public static Dependency from(Identifier identifier, License license, String repository) {
        return new Dependency(identifier, license, repository);
    }

}
