package com.openelements.oss.license.scanner.licenses;

import java.util.Set;

public interface LicenseConstants {

   Set<String> APACHE_2_0_URLS = Set.of("https://api.github.com/licenses/apache-2.0",
           "http://www.apache.org/licenses/LICENSE-2.0.txt",
           "https://www.apache.org/licenses/LICENSE-2.0.txt",
           "http://www.apache.org/licenses/LICENSE-2.0",
           "https://www.apache.org/licenses/LICENSE-2.0");

   Set<String> MIT_URLS = Set.of("https://api.github.com/licenses/mit");

    Set<String> BSD_3_CLAUSE_URLS = Set.of("https://api.github.com/licenses/bsd-3-clause");

    Set<String> BSD_2_CLAUSE_URLS = Set.of("https://api.github.com/licenses/bsd-2-clause");

    Set<String> EPL_2_0_URLS = Set.of("https://api.github.com/licenses/epl-2.0");

    Set<String> EPL_1_0_URLS = Set.of("https://www.eclipse.org/legal/epl-v10.html");

    Set<String> PUBLIC_DOMAIN_URLS = Set.of("https://api.github.com/licenses/unlicense");

    Set<String> GPL_3_0_URLS = Set.of("https://api.github.com/licenses/gpl-3.0");

    Set<String> GPL_2_0_URLS = Set.of("https://api.github.com/licenses/gpl-2.0");

    Set<String> LGPL_2_0_URLS = Set.of("https://api.github.com/licenses/lgpl-2.0");

    Set<String> MOZILLA_2_0_URLS = Set.of("https://api.github.com/licenses/mpl-2.0");

    Set<String> CC0_1_0_URLS = Set.of("https://api.github.com/licenses/cc0-1.0");

    Set<String> APACHE_2_0_NAMES = Set.of("Apache", "Apache-2.0");

    Set<String> MIT_NAMES = Set.of("MIT");

    Set<String> BSD_3_CLAUSE_NAMES = Set.of("BSD-3-Clause", "BSD-3");

    Set<String> BSD_2_CLAUSE_NAMES = Set.of("BSD-2-Clause", "BSD-2");

    Set<String> EPL_2_0_NAMES = Set.of("EPL-2.0", "EPL-2");

    Set<String> EPL_1_0_NAMES = Set.of("EPL-1.0", "EPL-1");

    Set<String> PUBLIC_DOMAIN_NAMES = Set.of("Public-Domain", "Unlicense");

    Set<String> GPL_3_0_NAMES = Set.of("GPL-3.0", "GPL-3");

    Set<String> GPL_2_0_NAMES = Set.of("GPL-2.0", "GPL-2");

    Set<String> LGPL_2_0_NAMES = Set.of("LGPL-2.0", "LGPL-2");

    Set<String> MOZILLA_2_0_NAMES = Set.of("MPL-2.0", "MPL-2");

    Set<String> CC0_1_0_NAMES = Set.of("CC0-1.0", "CC0-1");
}
