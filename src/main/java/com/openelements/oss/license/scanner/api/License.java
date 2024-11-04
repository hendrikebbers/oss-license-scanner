package com.openelements.oss.license.scanner.api;

import java.util.Objects;

public record License(String name, String url, String source) {

    public static final License UNKNOWN = new License("Unknown", "Unknown", "Unknown");

    public boolean isUnknown() {
        return this.equals(UNKNOWN);
    }


}
