package com.openelements.oss.license.scanner.api;

import java.util.Objects;

public record License(String name, String url, String source) {

    public static final License UNKNOWN = new License("Unknown", "Unknown", "Unknown");

    public boolean isUnknown() {
        return this.equals(UNKNOWN);
    }

    public boolean isApache() {
        if(Objects.equals(url, "https://api.github.com/licenses/apache-2.0")) {
            return true;
        }
        if(Objects.equals(url, "http://www.apache.org/licenses/LICENSE-2.0.txt")) {
            return true;
        }
        if(Objects.equals(url, "https://www.apache.org/licenses/LICENSE-2.0.txt")) {
            return true;
        }
        if(Objects.equals(url, "http://www.apache.org/licenses/LICENSE-2.0")) {
            return true;
        }
        if(Objects.equals(url, "https://www.apache.org/licenses/LICENSE-2.0")) {
            return true;
        }
        return false;
    }

}
