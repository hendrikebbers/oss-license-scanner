# oss-licence-scannerrwfer

A small tool that scans a library, repository or local directory for the licences of all (transitive) dependencies.

The tool has been created to help transfering all [Hedera](https://hedera.com) projects to [Hiero](https://hiero.org).
See https://github.com/hiero-ledger/hiero/blob/main/transition.md

## Usage

> [!IMPORTANT]  
> Today the tool is only working on unix based systems.

The tool is based on Java and therefore Java 21+ must be installed to run the tool.
We prefer to use Eclipse Temurin as Java distribution.
You use https://adoptium.net/ to download the latest version.

The easiest way to use the tool is by using the provided shell script `licence-scanner.sh`.
The tool provides multiple options to scan a library, repository or local directory.
Some examples how the tool can be used:

```shell

# Scans all the Java library com.hedera.hashgraph:app with version 0.55.2 and writes the result to the console
./license-scanner.sh  java -n com.hedera.hashgraph:app -v 0.55.2

# Scans all the Rust library from the repository and writes the result to a file
./license-scanner.sh  rust -r https://github.com/hashgraph/hedera-sdk-rust > rust-dependencies.csv

```

The first param is the language of the library. The following languages are supported:
- java (only maven)
- rust
- go
- python
- npm
- swift (only on macOS)

## Dependencies

To use the tool several local tools must be installed depending on the language of the library.
If a tool is missing an error will be shown.
