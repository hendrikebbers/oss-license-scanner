package com.openelements.oss.license.scanner.api;

public record Dependency(Identifier identifier, License license, String repository) {

}
