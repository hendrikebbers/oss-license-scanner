package com.openelements.oss.license.scanner.data;

import java.util.Objects;

public record License(String name, String url) {

    public static final License UNKNOWN = new License("Unknown", "Unknown");

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

    public boolean isMit() {
        return Objects.equals(url, "https://api.github.com/licenses/mit");
    }

    public boolean isBsd3() {
        return Objects.equals(url, "https://api.github.com/licenses/bsd-3-clause");
    }

    public boolean isBsd2() {
        return Objects.equals(url, "https://api.github.com/licenses/bsd-2-clause");
    }

    public boolean isEpl2() {
        return Objects.equals(url, "https://api.github.com/licenses/epl-2.0");
    }
}