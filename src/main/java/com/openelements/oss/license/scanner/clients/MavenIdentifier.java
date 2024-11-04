package com.openelements.oss.license.scanner.clients;

import com.openelements.oss.license.scanner.data.Identifier;

public record MavenIdentifier(String groupId, String artifactId, String version) {

    public MavenIdentifier(Identifier identifier) {
        this(identifier.name().split(":")[0], identifier.name().split(":")[1], identifier.version());
    }

    public MavenIdentifier(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Identifier toIdentifier() {
        return new Identifier(groupId + ":" + artifactId, version);
    }
}
