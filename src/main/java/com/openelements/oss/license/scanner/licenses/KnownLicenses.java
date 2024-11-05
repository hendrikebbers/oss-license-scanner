package com.openelements.oss.license.scanner.licenses;

import com.openelements.oss.license.scanner.api.License;
import java.util.Arrays;
import java.util.List;
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
    LGPL_2_1(LGPL_2_1_NAMES, LGPL_2_1_URLS),
    LGPL_3_0(LGPL_3_0_NAMES, LGPL_3_0_URLS),
    MOZILLA_2_0(MOZILLA_2_0_NAMES, MOZILLA_2_0_URLS),
    CC0_1_0(CC0_1_0_NAMES, CC0_1_0_URLS),
    ISC(ISC_NAMES, ISC_URLS);

    private final List<String> names;

    private final List<String> urls;

    KnownLicenses(List<String> names, List<String> urls) {
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

    public boolean matches(License license) {
        if(license.url() != null && urls.contains(license.url())) {
            return true;
        }
        if(license.name() != null) {
            return names.contains(license.name());
        }
        return false;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getName() {
        return getNames().get(0);
    }

    public String getUrl() {
        return getUrls().get(0);
    }
}
