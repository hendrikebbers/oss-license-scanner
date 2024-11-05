package com.openelements.oss.license.scanner.licenses;

import java.util.List;
import java.util.Set;

public interface LicenseConstants {

    List<String> APACHE_2_0_URLS = List.of("https://api.github.com/licenses/apache-2.0",
            "http://www.apache.org/licenses/LICENSE-2.0.txt",
            "https://www.apache.org/licenses/LICENSE-2.0.txt",
            "http://www.apache.org/licenses/LICENSE-2.0",
            "https://www.apache.org/licenses/LICENSE-2.0",
            "http://repository.jboss.org/licenses/apache-2.0.txt");

    List<String> MIT_URLS = List.of("https://api.github.com/licenses/mit",
            "https://opensource.org/licenses/MIT",
            "http://www.opensource.org/licenses/mit-license.php");

    List<String> BSD_3_CLAUSE_URLS = List.of("https://api.github.com/licenses/bsd-3-clause",
            "http://opensource.org/licenses/BSD-3-Clause");

    List<String> BSD_2_CLAUSE_URLS = List.of("https://api.github.com/licenses/bsd-2-clause");

    List<String> EPL_2_0_URLS = List.of("https://api.github.com/licenses/epl-2.0");

    List<String> EPL_1_0_URLS = List.of("https://api.github.com/licenses/epl-1.0",
            "https://www.eclipse.org/legal/epl-v10.html");

    List<String> PUBLIC_DOMAIN_URLS = List.of("https://api.github.com/licenses/unlicense");

    List<String> GPL_3_0_URLS = List.of("https://api.github.com/licenses/gpl-3.0");

    List<String> GPL_2_0_URLS = List.of("https://api.github.com/licenses/gpl-2.0");

    List<String> LGPL_2_0_URLS = List.of("https://api.github.com/licenses/lgpl-2.0");

    List<String> LGPL_2_1_URLS = List.of("https://api.github.com/licenses/lgpl-2.1", "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html");

    List<String> LGPL_3_0_URLS = List.of("https://api.github.com/licenses/lgpl-3.0");

    List<String> MOZILLA_2_0_URLS = List.of("https://api.github.com/licenses/mpl-2.0");

    List<String> CC0_1_0_URLS = List.of("https://api.github.com/licenses/cc0-1.0", "https://creativecommons.org/publicdomain/zero/1.0/");

    List<String> ISC_URLS = List.of("https://api.github.com/licenses/isc");

    List<String> APACHE_2_0_NAMES = List.of("Apache-2.0", "Apache","Apache License, Version 2.0", "Apache 2.0",
            "The Apache Software License, Version 2.0", "Apache 2.0 License", "Apache License 2.0");

    List<String> MIT_NAMES = List.of("MIT", "The MIT License (MIT)", "MIT License", "The MIT License", "MIT license");

    List<String> BSD_3_CLAUSE_NAMES = List.of("BSD-3-Clause", "BSD-3", "BSD 3-Clause", "BSD License 3",
            "BSD 3-Clause \"New\" or \"Revised\" License", "3-Clause BSD License");

    List<String> BSD_2_CLAUSE_NAMES = List.of("BSD-2-Clause", "BSD-2", "BSD 2-Clause \"Simplified\" License");

    List<String> EPL_2_0_NAMES = List.of("EPL-2.0", "EPL-2", "Eclipse Public License, Version 2.0", "Eclipse Public License v2.0", "EPL 2.0");

    List<String> EPL_1_0_NAMES = List.of("EPL-1.0", "EPL-1", "Eclipse Public License - v 1.0",
            "Eclipse Public License v1.0");

    List<String> PUBLIC_DOMAIN_NAMES = List.of("Public-Domain", "Unlicense");

    List<String> GPL_3_0_NAMES = List.of("GPL-3.0", "GPL-3");

    List<String> GPL_2_0_NAMES = List.of("GPL-2.0", "GPL-2");

    List<String> LGPL_2_0_NAMES = List.of("LGPL-2.0", "LGPL-2");

    List<String> LGPL_2_1_NAMES = List.of("LGPL-2.1", "GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1");

    List<String> LGPL_3_0_NAMES = List.of("LGPL-3.0", "LGPL-3", "GNU Lesser General Public License v3.0");

    List<String> MOZILLA_2_0_NAMES = List.of("MPL-2.0", "MPL-2", "Mozilla Public License, Version 2.0");

    List<String> CC0_1_0_NAMES = List.of("CC0-1.0", "CC0-1", "CC0 1.0 Universal");

    List<String> ISC_NAMES = List.of("ISC","ISC License");

}
