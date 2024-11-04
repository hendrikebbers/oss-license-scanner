package com.openelements.oss.license.scanner.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.openelements.oss.license.scanner.data.Identifier;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SwiftHelper {

    public record SwiftLib(Identifier identifier, String repositoryUrl) {
    }

    private static Set<SwiftLib> convertFromJson(final JsonObject jsonObject) {
        final Set<SwiftLib> dependencies = new HashSet<>();
        final String name = jsonObject.get("name").getAsString();
        final String version = jsonObject.get("version").getAsString();
        final Identifier identifier = new Identifier(name, version);
        final String githubUrl = jsonObject.get("url").getAsString();
        if(jsonObject.has("dependencies")) {
            jsonObject.get("dependencies").getAsJsonArray().forEach(d -> {
                dependencies.addAll(convertFromJson(d.getAsJsonObject()));
            });
        }
        dependencies.add(new SwiftLib(identifier, githubUrl));
        return Collections.unmodifiableSet(dependencies);
    }

    public static Set<SwiftLib> callShowDependencies(Path pathToProject) {
        final JsonElement jsonElement = ProcessHelper.executeWithJsonResult(pathToProject.toFile(), "swift", "package",
                "show-dependencies", "--format", "json");

        final Set<SwiftLib> dependencies = new HashSet<>();
        jsonElement.getAsJsonObject().get("dependencies").getAsJsonArray().forEach(d -> {
           dependencies.addAll(convertFromJson(d.getAsJsonObject()));
        });
        return Collections.unmodifiableSet(dependencies);
    }
}
