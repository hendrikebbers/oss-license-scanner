package com.openelements.oss.license.scanner.tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.data.Dependency;
import com.openelements.oss.license.scanner.data.Identifier;
import com.openelements.oss.license.scanner.data.License;
import com.openelements.oss.license.scanner.resolver.NpmResolver;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NpmHelper {

    private final static Logger log = LoggerFactory.getLogger(NpmHelper.class);


    public static final JsonObject callNpmLs(Path pathToProject) {
        NpmHelper.executeNpmInstall(pathToProject);
        return ProcessHelper.executeWithResult(l -> npmOutToJson(l),pathToProject.toFile(),  "npm", "ls", "--production", "--json");
    }

    private static JsonObject npmOutToJson(List<String> lines) {
        StringReader reader = new StringReader(lines.stream().collect(Collectors.joining("\n")));
        return JsonParser.parseReader(reader).getAsJsonObject();
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
            return Optional.of(repository);
        }
    }

    public static Optional<String> callNpmShowAndReturnRepository(Identifier identifier) {
        return ProcessHelper.executeWithResult(l -> extractRepositoryFromNpmShowOut(l), "npm", "show", identifier.name() + "@" + identifier.version(), "--json");
    }

    public static void executeNpmInstall(Path pathToProject) {
        ProcessHelper.execute(l -> l.forEach(log::debug), pathToProject.toFile(), "npm", "install");
    }

}
