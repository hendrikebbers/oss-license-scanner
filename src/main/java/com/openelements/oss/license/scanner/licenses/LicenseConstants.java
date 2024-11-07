package com.openelements.oss.license.scanner.licenses;

import java.util.Set;

public interface LicenseConstants {

    String APACHE_2_0_SPDX_URL = "https://spdx.org/licenses/Apache-2.0.html";

    String APACHE_2_0_GITHUB_URL = "https://api.github.com/licenses/apache-2.0";

    Set<String> APACHE_2_0_ALTERNATIVE_URLS = Set.of(
            "http://www.apache.org/licenses/LICENSE-2.0.txt",
            "https://www.apache.org/licenses/LICENSE-2.0.txt",
            "http://www.apache.org/licenses/LICENSE-2.0",
            "https://www.apache.org/licenses/LICENSE-2.0",
            "http://repository.jboss.org/licenses/apache-2.0.txt", "https://opensource.org/licenses/Apache-2.0");

    String MIT_SPDX_URL = "https://spdx.org/licenses/MIT.html";

    String MIT_GITHUB_URL = "https://api.github.com/licenses/mit";

    Set<String> MIT_ALTERNATIVE_URLS = Set.of("https://opensource.org/licenses/MIT",
            "http://www.opensource.org/licenses/mit-license.php");

    String BSD_3_CLAUSE_SPDX_URL = "https://spdx.org/licenses/BSD-3-Clause.html";

    String BSD_3_CLAUSE_GITHUB_URL = "https://api.github.com/licenses/bsd-3-clause";

    Set<String> BSD_3_CLAUSE_ALTERNATIVE_URLS = Set.of("http://opensource.org/licenses/BSD-3-Clause", "https://api.github.com/licenses/edl-1.0", "https://www.eclipse.org/org/documents/edl-v10.php");

    String BSD_2_CLAUSE_SPDX_URL = "https://spdx.org/licenses/BSD-2-Clause.html";

    String BSD_2_CLAUSE_GITHUB_URL = "https://api.github.com/licenses/bsd-2-clause";

    Set<String> BSD_2_CLAUSE_ALTERNATIVE_URLS = Set.of("https://opensource.org/licenses/BSD-2-Clause");

    String EPL_2_0_SPDX_URL = "https://spdx.org/licenses/EPL-2.0.html";

    String EPL_2_0_GITHUB_URL = "https://api.github.com/licenses/epl-2.0";

    Set<String> EPL_2_0_URLS = Set.of("https://www.eclipse.org/legal/epl-2.0", "https://www.opensource.org/licenses/EPL-2.0");

    String EPL_1_0_SPDX_URL = "https://spdx.org/licenses/EPL-1.0.html";

    String EPL_1_0_GITHUB_URL = "https://api.github.com/licenses/epl-1.0";

    Set<String> EPL_1_0_URLS = Set.of("https://www.eclipse.org/legal/epl-v10.html", "https://opensource.org/licenses/EPL-1.0");

    String UNLICENSE_SPDX_URL = "https://spdx.org/licenses/Unlicense.html";

    String UNLICENSE_GITHUB_URL = "https://api.github.com/licenses/unlicense";

    Set<String> UNLICENSE_URLS = Set.of("https://unlicense.org/", "https://opensource.org/license/unlicense");

    String GPL_3_0_SPDX_URL = "https://spdx.org/licenses/GPL-3.0.html";

    String GPL_3_0_GITHUB_URL = "https://api.github.com/licenses/gpl-3.0";

    Set<String> GPL_3_0_URLS = Set.of("https://www.gnu.org/licenses/gpl-3.0-standalone.html", "https://opensource.org/licenses/GPL-3.0");

    String GPL_2_0_SPDX_URL = "https://spdx.org/licenses/GPL-2.0.html";

    String GPL_2_0_GITHUB_URL = "https://api.github.com/licenses/gpl-2.0";

    Set<String> GPL_2_0_URLS = Set.of("https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html", "https://opensource.org/licenses/GPL-2.0");

    String LGPL_2_0_SPDX_URL = "https://spdx.org/licenses/LGPL-2.0.html";

    String LGPL_2_0_GITHUB_URL = "https://api.github.com/licenses/lgpl-2.0";

    Set<String> LGPL_2_0_URLS = Set.of("https://opensource.org/license/lgpl-2-0");

    String LGPL_2_1_SPDX_URL = "https://spdx.org/licenses/LGPL-2.1.html";

    String LGPL_2_1_GITHUB_URL = "https://api.github.com/licenses/lgpl-2.1";

    Set<String> LGPL_2_1_URLS = Set.of("https://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html", "https://opensource.org/licenses/LGPL-2.1");

    String LGPL_3_0_SPDX_URL = "https://spdx.org/licenses/LGPL-3.0.html";

    String LGPL_3_0_GITHUB_URL = "https://api.github.com/licenses/lgpl-3.0";

    Set<String> LGPL_3_0_URLS = Set.of("https://www.gnu.org/licenses/lgpl-3.0-standalone.html", "https://www.gnu.org/licenses/lgpl+gpl-3.0.txt", "https://opensource.org/licenses/LGPL-3.0");

    String MPL_2_0_SPDX_URL = "https://spdx.org/licenses/MPL-2.0.html";

    String MPL_2_0_GITHUB_URL = "https://api.github.com/licenses/mpl-2.0";

    Set<String> MPL_2_0_URLS = Set.of("https://www.mozilla.org/MPL/2.0/", "https://opensource.org/licenses/MPL-2.0");

    String CC0_1_0_SPDX_URL = "https://spdx.org/licenses/CC0-1.0.html";

    String CC0_1_0_GITHUB_URL = "https://api.github.com/licenses/cc0-1.0";

    Set<String> CC0_1_0_URLS = Set.of("https://creativecommons.org/publicdomain/zero/1.0/legalcode");

    String ISC_SPDX_URL = "https://spdx.org/licenses/ISC.html";

    String ISC_GITHUB_URL = "https://api.github.com/licenses/isc";

    Set<String> ISC_URLS = Set.of("https://www.isc.org/downloads/software-support-policy/isc-license/", "https://www.isc.org/licenses/", "https://opensource.org/licenses/ISC");

    String CDDL_1_0_SPDX_URL = "https://spdx.org/licenses/CDDL-1.0.html";

    String CDDL_1_0_GITHUB_URL = "https://api.github.com/licenses/cddl-1.0";

    Set<String> CDDL_1_0_URLS = Set.of("http://www.sun.com/cddl/cddl.html", "https://opensource.org/license/cddl-1-0");


    String APACHE_2_0_SPDX_ID = "Apache-2.0";

    Set<String> APACHE_2_0_ALTERNATIVE_NAMES = Set.of("Apache","Apache License, Version 2.0", "Apache 2.0",
            "The Apache Software License, Version 2.0", "Apache 2.0 License", "Apache License 2.0");

    String MIT_SPDX_ID = "MIT";

    Set<String> MIT_ALTERNATIVE_NAMES = Set.of("The MIT License (MIT)", "MIT License", "The MIT License", "MIT license");

    String BSD_3_CLAUSE_SPDX_ID = "BSD-3-Clause";

    Set<String> BSD_3_CLAUSE_ALTERNATIVE_NAMES = Set.of("BSD-3", "BSD 3-Clause", "BSD License 3",
            "BSD 3-Clause \"New\" or \"Revised\" License", "3-Clause BSD License", "EDL 1.0", "Eclipse Distribution License - v 1.0");

    String BSD_2_CLAUSE_SPDX_ID = "BSD-2-Clause";

    Set<String> BSD_2_CLAUSE_ALTERNATIVE_NAMES = Set.of("BSD-2", "BSD 2-Clause \"Simplified\" License");

    String EPL_2_0_SPDX_ID = "EPL-2.0";

    Set<String> EPL_2_0_ALTERNATIVE_NAMES = Set.of("EPL-2", "Eclipse Public License, Version 2.0", "Eclipse Public License v2.0", "EPL 2.0", "Eclipse Public License 2.0", "Eclipse Public License v. 2.0");

    String EPL_1_0_SPDX_ID = "EPL-1.0";

    Set<String> EPL_1_0_ALTERNATIVE_NAMES = Set.of("EPL-1", "Eclipse Public License - v 1.0",
            "Eclipse Public License v1.0");

    String UNLICENSE_SPDX_ID = "Unlicense";

    Set<String> UNLICENSE_ALTERNATIVE_NAMES = Set.of("The Unlicense");

    String GPL_3_0_SPDX_ID = "GPL-3.0";

    Set<String> GPL_3_0_ALTERNATIVE_NAMES = Set.of("GPL-3");

    String GPL_2_0_SPDX_ID = "GPL-2.0";

    Set<String> GPL_2_0_ALTERNATIVE_NAMES = Set.of("GPL-2");

    String LGPL_2_0_SPDX_ID = "LGPL-2.0";

    Set<String> LGPL_2_0_ALTERNATIVE_NAMES = Set.of("LGPL-2");

    String LGPL_2_1_SPDX_ID = "LGPL-2.1";

    Set<String> LGPL_2_1_ALTERNATIVE_NAMES = Set.of("GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1");

    String LGPL_3_0_SPDX_ID = "LGPL-3.0";

    Set<String> LGPL_3_0_ALTERNATIVE_NAMES = Set.of("LGPL-3", "GNU Lesser General Public License v3.0");

    String MPL_2_0_SPDX_ID = "MPL-2.0";

    Set<String> MPL_2_0_ALTERNATIVE_NAMES = Set.of("MPL-2", "Mozilla Public License, Version 2.0");

    String CC0_1_0_SPDX_ID = "CC0-1.0";

    Set<String> CC0_1_0_ALTERNATIVE_NAMES = Set.of("CC0-1", "CC0 1.0 Universal");

    String ISC_SPDX_ID = "ISC";

    Set<String> ISC_ALTERNATIVE_NAMES = Set.of("ISC License", "ISC-Lizenz");

    String CDDL_1_0_SPDX_ID = "CDDL-1.0";

    Set<String> CDDL_1_0_ALTERNATIVE_NAMES = Set.of();


}
