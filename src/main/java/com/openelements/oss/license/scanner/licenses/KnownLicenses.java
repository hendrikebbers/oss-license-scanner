package com.openelements.oss.license.scanner.licenses;

import com.openelements.oss.license.scanner.api.License;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public enum KnownLicenses implements LicenseConstants {
    APACHE_2_0(APACHE_2_0_SPDX_ID, APACHE_2_0_ALTERNATIVE_NAMES, APACHE_2_0_SPDX_URL, APACHE_2_0_GITHUB_URL, APACHE_2_0_ALTERNATIVE_URLS),
    MIT(MIT_SPDX_ID, MIT_ALTERNATIVE_NAMES, MIT_SPDX_URL, MIT_GITHUB_URL, MIT_ALTERNATIVE_URLS),
    BSD_3_CLAUSE(BSD_3_CLAUSE_SPDX_ID, BSD_3_CLAUSE_ALTERNATIVE_NAMES, BSD_3_CLAUSE_SPDX_URL, BSD_3_CLAUSE_GITHUB_URL, BSD_3_CLAUSE_ALTERNATIVE_URLS),
    BSD_2_CLAUSE(BSD_2_CLAUSE_SPDX_ID, BSD_2_CLAUSE_ALTERNATIVE_NAMES, BSD_2_CLAUSE_SPDX_URL, BSD_2_CLAUSE_GITHUB_URL, BSD_2_CLAUSE_ALTERNATIVE_URLS),
    EPL_2_0(EPL_2_0_SPDX_ID, EPL_2_0_ALTERNATIVE_NAMES, EPL_2_0_SPDX_URL, EPL_2_0_GITHUB_URL, EPL_2_0_URLS),
    EPL_1_0(EPL_1_0_SPDX_ID, EPL_1_0_ALTERNATIVE_NAMES, EPL_1_0_SPDX_URL, EPL_1_0_GITHUB_URL,  EPL_1_0_URLS),
    UNLICENSE(UNLICENSE_SPDX_ID, UNLICENSE_ALTERNATIVE_NAMES, UNLICENSE_SPDX_URL, UNLICENSE_GITHUB_URL, UNLICENSE_URLS),
    GPL_3_0(GPL_3_0_SPDX_ID, GPL_3_0_ALTERNATIVE_NAMES, GPL_3_0_SPDX_URL, GPL_3_0_GITHUB_URL,  GPL_3_0_URLS),
    GPL_2_0(GPL_2_0_SPDX_ID, GPL_2_0_ALTERNATIVE_NAMES, GPL_2_0_SPDX_URL, GPL_2_0_GITHUB_URL, GPL_2_0_URLS),
    LGPL_2_0(LGPL_2_0_SPDX_ID, LGPL_2_0_ALTERNATIVE_NAMES, LGPL_2_0_SPDX_URL, LGPL_2_0_GITHUB_URL,  LGPL_2_0_URLS),
    LGPL_2_1(LGPL_2_1_SPDX_ID, LGPL_2_1_ALTERNATIVE_NAMES, LGPL_2_1_SPDX_URL, LGPL_2_1_GITHUB_URL, LGPL_2_1_URLS),
    LGPL_3_0(LGPL_3_0_SPDX_ID, LGPL_3_0_ALTERNATIVE_NAMES, LGPL_3_0_SPDX_URL, LGPL_3_0_GITHUB_URL, LGPL_3_0_URLS),
    MPL_2_0(MPL_2_0_SPDX_ID, MPL_2_0_ALTERNATIVE_NAMES, MPL_2_0_SPDX_URL, MPL_2_0_GITHUB_URL, MPL_2_0_URLS),
    CC0_1_0(CC0_1_0_SPDX_ID, CC0_1_0_ALTERNATIVE_NAMES, CC0_1_0_SPDX_URL, CC0_1_0_GITHUB_URL, CC0_1_0_URLS),
    ISC(ISC_SPDX_ID, ISC_ALTERNATIVE_NAMES, ISC_SPDX_URL, ISC_GITHUB_URL,  ISC_URLS),
    CCDL_1_0(CDDL_1_0_SPDX_ID, CDDL_1_0_ALTERNATIVE_NAMES, CDDL_1_0_SPDX_URL, CDDL_1_0_GITHUB_URL, CDDL_1_0_URLS);

    private final String spdxId;

    private final Set<String> names;

    private final String spdxUrl;

    private final String gitHubUrl;

    private final Set<String> urls;

    KnownLicenses(String spdxId, Set<String> names, String spdxUrl, String gitHubUrl, Set<String> urls) {
        this.spdxId = spdxId;
        this.spdxUrl = spdxUrl;
        this.gitHubUrl = gitHubUrl;

        final Set<String> allNames = new HashSet<>();
        allNames.add(spdxId);
        allNames.addAll(names);
        this.names = Collections.unmodifiableSet(allNames);
        final Set<String> allUrls = new HashSet<>();
        allUrls.add(spdxUrl);
        allUrls.add(gitHubUrl);
        allUrls.addAll(urls);
        this.urls = Collections.unmodifiableSet(allUrls);
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

    public Set<String> getNames() {
        return names;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public String getSpdxUrl() {
        return spdxUrl;
    }

    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public String getSpdxId() {
        return spdxId;
    }
}
