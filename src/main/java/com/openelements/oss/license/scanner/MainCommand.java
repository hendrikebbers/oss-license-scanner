package com.openelements.oss.license.scanner;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(description = "A tool to scan a library for all dependencies", subcommands = {JavaCommand.class, JavaScriptCommand.class, RustCommand.class, SwiftCommand.class, GoCommand.class})
public class MainCommand {

    public static void main(String[] args) {
        new CommandLine(new MainCommand()).execute(args);
    }
}
