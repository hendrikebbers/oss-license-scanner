package com.openelements.oss.license.scanner.cache;

import com.openelements.oss.license.scanner.api.Identifier;

public record CacheIdentifier(String language, Identifier identifier) {
}
