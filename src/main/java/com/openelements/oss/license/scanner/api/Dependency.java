package com.openelements.oss.license.scanner.api;

import java.util.Objects;

public record Dependency(Identifier identifier, License license, String repository,
                         DependencyType dependencyType) implements Comparable<Dependency> {

    public Dependency(Identifier identifier, License license, String repository, DependencyType dependencyType) {
        this.identifier = Objects.requireNonNull(identifier);
        this.license = Objects.requireNonNull(license);
        this.repository = repository;
        this.dependencyType = Objects.requireNonNull(dependencyType);
    }

    public Dependency(Identifier identifier, License license, String repository) {
        this(identifier, license, repository, DependencyType.UNKNOWN);
    }

    @Override
    public int compareTo(Dependency o) {
        return identifier.compareTo(o.identifier);
    }

    public static Dependency from(Identifier identifier, License license, String repository) {
        return new Dependency(identifier, license, repository);
    }

}
