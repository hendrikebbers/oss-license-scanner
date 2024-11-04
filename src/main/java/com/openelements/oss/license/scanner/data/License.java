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
        if(Objects.equals(url, "https://asm.ow2.io/license.html")) {
            return true;
        }
        return Objects.equals(url, "https://api.github.com/licenses/bsd-3-clause");
    }

    public boolean isBsd2() {
        return Objects.equals(url, "https://api.github.com/licenses/bsd-2-clause");
    }

    public boolean isEpl2() {
        return Objects.equals(url, "https://api.github.com/licenses/epl-2.0");
    }

    public boolean isEpl1() {
        return Objects.equals(url, "https://www.eclipse.org/legal/epl-v10.html");
    }


    public boolean isPublicDomain() {
        return Objects.equals(url, "https://api.github.com/licenses/unlicense");
    }

    public boolean isGpl3() {
        return Objects.equals(url, "https://api.github.com/licenses/gpl-3.0");
    }

    public boolean isGpl2() {
        return Objects.equals(url, "https://api.github.com/licenses/gpl-2.0");
    }

    public boolean isLgpl3() {
        return Objects.equals(url, "https://api.github.com/licenses/lgpl-3.0");
    }

    public boolean isLgpl2() {
        return Objects.equals(url, "https://api.github.com/licenses/lgpl-2.1");
    }

    public boolean isMozilla() {
        return Objects.equals(url, "https://api.github.com/licenses/mpl-2.0");
    }

    public boolean isCddl() {
        return Objects.equals(url, "https://api.github.com/licenses/cddl-1.0");
    }

}
