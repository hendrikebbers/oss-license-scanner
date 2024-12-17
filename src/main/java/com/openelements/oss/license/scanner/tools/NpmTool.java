package com.openelements.oss.license.scanner.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.clients.GitHubClient;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmTool {

    private final static Logger log = LoggerFactory.getLogger(NpmTool.class);

    public static final JsonObject callPnpmList(Path pathToProject) {
        if (!ProcessHelper.checkCommand("npm")) {
            throw new RuntimeException("npm command is not installed");
        }
        installPnpm(pathToProject);
        callPnpmInstall(pathToProject);

        final JsonElement element = ProcessHelper.executeWithJsonResult(pathToProject.toFile(), "pnpm", "list",
                "--prod",
                "--json");
        if (element.isJsonArray()) {
            if (element.getAsJsonArray().size() != 1) {
                throw new IllegalStateException("Expected a single JsonArray but got: " + element);
            }
            return element.getAsJsonArray().get(0).getAsJsonObject();
        } else {
            throw new IllegalStateException("Expected a JsonArray but got: " + element);
        }
    }

    public static final void installPnpm(Path pathToProject) {
        if (!ProcessHelper.checkCommand("npm")) {
            throw new RuntimeException("npm command is not installed");
        }
        ProcessHelper.execute(l -> l.forEach(log::debug), pathToProject.toFile(), "npm", "install", "-g", "pnpm");
    }

    public static final void callPnpmInstall(Path pathToProject) {
        if (!ProcessHelper.checkCommand("pnpm")) {
            throw new RuntimeException("pnpm command is not installed");
        }
        ProcessHelper.execute(l -> l.forEach(log::debug), pathToProject.toFile(), "pnpm", "install");
    }

    public static final JsonObject callNpmLs(Path pathToProject) {
        if (!ProcessHelper.checkCommand("npm")) {
            throw new RuntimeException("npm command is not installed");
        }
        NpmTool.executeNpmInstall(pathToProject);
        final JsonObject result = ProcessHelper.executeWithResult(l -> npmOutToJson(l), pathToProject.toFile(), "npm",
                "ls", "--production", "--json");
        if (result.has("error")) {
            throw new IllegalStateException("Error in npm ls: " + result.get("error").getAsString());
        }
        return result;
    }

    private static JsonObject npmOutToJson(List<String> lines) {
        StringReader reader = new StringReader(lines.stream().collect(Collectors.joining("\n")));
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    private static Optional<License> extractLicenseFromNpmShowOut(List<String> lines) {
        final JsonObject jsonObject = npmOutToJson(lines);
        if (!jsonObject.has("license")) {
            return Optional.empty();
        } else {
            final String licenceName = jsonObject.get("license").getAsString();
            return Optional.of(new License(licenceName, "UNKNOWN", "npm show"));
        }
    }

    private static Optional<String> extractRepositoryFromNpmShowOut(List<String> lines) {
        final JsonObject jsonObject = npmOutToJson(lines);
        if (!jsonObject.has("repository")) {
            return Optional.empty();
        } else if (!jsonObject.get("repository").isJsonObject()) {
            return Optional.empty();
        } else if (!jsonObject.get("repository").getAsJsonObject().has("url")) {
            return Optional.empty();
        } else {
            final String repository = jsonObject.get("repository").getAsJsonObject().get("url").getAsString();
            final String normalizedUrl = GitHubClient.normalizeUrl(repository);
            return Optional.of(normalizedUrl);
        }
    }

    public static Optional<String> callNpmShowAndReturnRepository(Identifier identifier) {
        if (!ProcessHelper.checkCommand("npm")) {
            throw new RuntimeException("npm command is not installed");
        }
        return ProcessHelper.executeWithResult(l -> extractRepositoryFromNpmShowOut(l), "npm", "show",
                identifier.name() + "@" + identifier.version(), "--json");
    }

    public static Optional<License> callNpmShowAndReturnLicense(Identifier identifier) {
        if (!ProcessHelper.checkCommand("npm")) {
            throw new RuntimeException("npm command is not installed");
        }
        return ProcessHelper.executeWithResult(l -> extractLicenseFromNpmShowOut(l), "npm", "show",
                identifier.name() + "@" + identifier.version(), "--json");
    }

    public static void executeNpmInstall(Path pathToProject) {
        if (!ProcessHelper.checkCommand("npm")) {
            throw new RuntimeException("npm command is not installed");
        }
        ProcessHelper.execute(l -> l.forEach(log::debug), pathToProject.toFile(), "npm", "install");
    }

}
