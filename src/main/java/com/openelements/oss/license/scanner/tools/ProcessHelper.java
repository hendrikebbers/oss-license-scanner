package com.openelements.oss.license.scanner.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHelper {

    private final static Logger log = LoggerFactory.getLogger(ProcessHelper.class);

    public static void main(String[] args) {
        if(checkCommand("mvn")) {
            System.out.println("Maven is installed");
        } else {
            System.out.println("Maven is not installed");
        }
        if(checkCommand("mvn2")) {
            System.out.println("Maven2 is installed");
        } else {
            System.out.println("Maven2 is not installed");
        }

    }

    public static boolean isUnixBased() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("nix") || osName.contains("nux") || osName.contains("mac");
    }

    public static boolean checkCommand(final String command) {
        if(!isUnixBased()) {
            throw new RuntimeException("Only Unix based systems are supported");
        }
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("command", "-v", command);
            final Process process = processBuilder.start();
            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error in checking command " + command, e);
        }
    }

    public static void execute(Consumer<List<String>> inputHandler, String... command) {
        execute(inputHandler, null, command);
    }

    public static void execute(Consumer<List<String>> inputHandler, File directory, String... command) {
        Function<List<String>, Void> converter = l -> {
            inputHandler.accept(l);
            return null;
        };
        executeWithResult(converter, directory, command);
    }

    public static <T> T executeWithResult(Function<List<String>, T> inputHandler, String... command) {
        return executeWithResult(inputHandler, null, command);
    }

    private static ExecutorService outputExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    public static <T> T executeWithResult(Function<List<String>, T> inputHandler, File directory, String... command) {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder(command);
            if(directory != null) {
                processBuilder.directory(directory);
            }
            final Process process = processBuilder.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            final List<String> out = new ArrayList<>();
            final List<String> errorOut = new ArrayList<>();
            try {
                final Future<?> outFuture = outputExecutor.submit(() -> {
                    reader.lines().forEach(l -> out.add(l));
                });
                final Future<?> errorFuture = outputExecutor.submit(() -> {
                    errorReader.lines().forEach(l -> errorOut.add(l));
                });
                outFuture.get();
                errorFuture.get();
                process.waitFor(10, TimeUnit.MINUTES);
            } finally {
                final int exitCode = process.waitFor();
                if (exitCode != 0) {
                    errorOut.forEach(l -> log.error(l));
                    new RuntimeException("Process exited with bad exit code: " + exitCode);
                }
            }
            return inputHandler.apply(Collections.unmodifiableList(out));
        } catch (Exception e) {
            throw new RuntimeException("Error in executing " + Arrays.toString(command), e);
        }
    }

    public static JsonElement executeWithJsonResult(File directory, String... command) {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder(command);
            if(directory != null) {
                processBuilder.directory(directory);
            }
            final Process process = processBuilder.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                return JsonParser.parseReader(reader);
            } finally {
                final int exitCode = process.waitFor();
                if (exitCode != 0) {
                    errorReader.lines().forEach(l -> log.error(l));
                    new RuntimeException("Process exited with bad exit code: " + exitCode);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in executing " + Arrays.toString(command), e);
        }
    }
}
