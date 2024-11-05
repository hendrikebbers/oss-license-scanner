#!/bin/bash

mkdir result
./license-scanner.sh  java -n com.hedera.hashgraph:app -v 0.55.2 -m hedera-manual.csv -N > result/consensus-node.csv
./license-scanner.sh  java -n com.hedera.hashgraph:sdk -v 2.43.0 -m hedera-manual.csv -N > result/sdk-java.csv
./license-scanner.sh  java -n com.hedera.hashgraph:did-sdk-java -v 1.0.0 -m hedera-manual.csv -N > result/did-sdk-java.csv
./license-scanner.sh  java -r https://github.com/OpenElements/hedera-enterprise -m hedera-manual.csv -N > result/hedera-enterprise.csv
./license-scanner.sh  rust -r https://github.com/hashgraph/hedera-sdk-rust -m hedera-manual.csv -N > result/sdk-rust.csv
./license-scanner.sh  swift -r https://github.com/hashgraph/hedera-sdk-swift -m hedera-manual.csv -N > result/sdk-swift.csv
./license-scanner.sh  go -r https://github.com/hashgraph/hedera-sdk-go -m hedera-manual.csv -N > result/sdk-go.csv
./license-scanner.sh  js -r https://github.com/hashgraph/hedera-sdk-js -m hedera-manual.csv -N > result/sdk-js.csv
./license-scanner.sh  js -r https://github.com/hashgraph/solo -m hedera-manual.csv -N > result/solo.csv
./license-scanner.sh  js -r https://github.com/hashgraph/hedera-local-node -m hedera-manual.csv -N > result/local-node.csv
./license-scanner.sh  js -r https://github.com/hashgraph/hedera-mirror-node-explorer -m hedera-manual.csv -N > result/mirror-node-explorer.csv
./license-scanner.sh  js -r https://github.com/hashgraph/hedera-json-rpc-relay -m hedera-manual.csv -N > result/json-rpc-relay.csv
./license-scanner.sh  js -r https://github.com/gomintco/gomint-api -m hedera-manual.csv -N > result/gomint-api.csv
./license-scanner.sh  js -r https://github.com/gomintco/gomint-client -m hedera-manual.csv -N > result/gomint-client.csv
./license-scanner.sh  python -r https://github.com/wensheng/hedera-sdk-py -m hedera-manual.csv -N > result/sdk-python-1.csv
./license-scanner.sh  python -r https://github.com/nadineloepfe/hedera_sdk_python -m hedera-manual.csv -N > result/sdk-python-2.csv

