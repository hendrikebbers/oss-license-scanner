package com.openelements.oss.license.scanner.licenses;

import com.openelements.oss.license.scanner.api.License;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public enum KnownLicenses implements LicenseConstants {
    APACHE_2_0(APACHE_2_0_NAMES, APACHE_2_0_URLS),
    MIT(MIT_NAMES, MIT_URLS),
    BSD_3_CLAUSE(BSD_3_CLAUSE_NAMES, BSD_3_CLAUSE_URLS),
    BSD_2_CLAUSE(BSD_2_CLAUSE_NAMES, BSD_2_CLAUSE_URLS),
    EPL_2_0(EPL_2_0_NAMES, EPL_2_0_URLS),
    EPL_1_0(EPL_1_0_NAMES, EPL_1_0_URLS),
    PUBLIC_DOMAIN(PUBLIC_DOMAIN_NAMES, PUBLIC_DOMAIN_URLS),
    GPL_3_0(GPL_3_0_NAMES, GPL_3_0_URLS),
    GPL_2_0(GPL_2_0_NAMES, GPL_2_0_URLS),
    LGPL_2_0(LGPL_2_0_NAMES, LGPL_2_0_URLS),
    MOZILLA_2_0(MOZILLA_2_0_NAMES, MOZILLA_2_0_URLS),
    CC0_1_0(CC0_1_0_NAMES, CC0_1_0_URLS);

    private final Set<String> names;

    private final Set<String> urls;

    KnownLicenses(Set<String> names, Set<String> urls) {
        this.names = names;
        this.urls = urls;
    }

    public Optional<KnownLicenses> forName(String name) {
        return Arrays.asList(KnownLicenses.values()).stream()
                .filter(license -> license.names.contains(name))
                .findFirst();
    }

    public static Optional<KnownLicenses> of(License license) {
        return Arrays.asList(KnownLicenses.values()).stream()
                .filter(knownLicense -> knownLicense.matches(license))
                .findFirst();
    }

    public boolean matches(String url) {
        return urls.contains(url);
    }

    public boolean matches(License license) {
        return matches(license.url());
    }

    public Set<String> getNames() {
        return names;
    }

    public Set<String> getUrls() {
        return urls;
    }
}
