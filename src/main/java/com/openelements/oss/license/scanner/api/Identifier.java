package com.openelements.oss.license.scanner.api;

public record Identifier(String name, String version) implements Comparable<Identifier> {

    @Override
    public int compareTo(Identifier o) {
        final int nameComparison = name.compareTo(o.name);
        if (nameComparison != 0) {
            return nameComparison;
        }
        return version.compareTo(o.version);
    }

    public static Identifier from(String name, String version) {
        return new Identifier(name, version);
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
