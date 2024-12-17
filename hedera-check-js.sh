#!/bin/bash

mkdir missing
./license-scanner.sh  js -r https://github.com/hashgraph/hedera-sdk-js -m manual-additions.csv -N -e Apache-2.0 -e MIT -e BSD-3-Clause -e BSD-2-Clause -e EPL-2.0 -e EPL-1.0 -e MPL-2.0 -e CDDL-1.0 -e EDL-1.0 -e Unlicense -e ISC > missing/sdk-js.csv
./license-scanner.sh  js -r https://github.com/hiero-ledger/hiero-sdk-tck -m manual-additions.csv -N -e Apache-2.0 -e MIT -e BSD-3-Clause -e BSD-2-Clause -e EPL-2.0 -e EPL-1.0 -e MPL-2.0 -e CDDL-1.0 -e EDL-1.0 -e Unlicense -e ISC > missing/sdk-tck.csv
./license-scanner.sh  js -r https://github.com/hashgraph/hedera-local-node -m manual-additions.csv -N -e Apache-2.0 -e MIT -e BSD-3-Clause -e BSD-2-Clause -e EPL-2.0 -e EPL-1.0 -e MPL-2.0 -e CDDL-1.0 -e EDL-1.0 -e Unlicense -e ISC > missing/local-node.csv
