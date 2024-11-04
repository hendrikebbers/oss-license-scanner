#!/bin/bash

#./license-scanner.sh  java -n com.hedera.hashgraph:app -v 0.55.2 -excludeLicenses Apache,MIT,BSD-3,BSD-2,EPL-2,EPL-1 > consensus-node.csv
#./license-scanner.sh  java -n com.hedera.hashgraph:sdk -v 2.43.0 -excludeLicenses Apache,MIT,BSD-3,BSD-2,EPL-2,EPL-1 > sdk-java.csv
#./license-scanner.sh  java -n com.hedera.hashgraph:did-sdk-java -v 1.0.0 -excludeLicenses Apache,MIT,BSD-3,BSD-2,EPL-2,EPL-1 > did-sdk-java.csv

mkdir result
./license-scanner.sh  java -n com.hedera.hashgraph:app -v 0.55.2 > result/consensus-node.csv
./license-scanner.sh  java -n com.hedera.hashgraph:sdk -v 2.43.0 > result/sdk-java.csv
./license-scanner.sh  java -n com.hedera.hashgraph:did-sdk-java -v 1.0.0 > result/did-sdk-java.csv
./license-scanner.sh  rust -n hedera -v 0.29.0 > result/sdk-rust.csv
./license-scanner.sh  swift -n https://github.com/hashgraph/hedera-sdk-swift -v 0.32.0 > result/sdk-swift.csv
./license-scanner.sh  js -n @hashgraph/sdk -v 2.52.0 > result/sdk-js.csv
./license-scanner.sh  js -n @hashgraph/solo -v 0.31.4 > result/solo.csv
./license-scanner.sh  js -n @hashgraph/hedera-local -v 2.31.0 > result/local-node.csv
