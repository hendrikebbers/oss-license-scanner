package com.openelements.oss.license.scanner.api;

public record License(String name, String url, String source) {

    public static final License UNKNOWN = new License(ApiConstants.UNKNOWN, ApiConstants.UNKNOWN, ApiConstants.UNKNOWN);

    public boolean isUnknown() {
        return this.equals(UNKNOWN);
    }

}
