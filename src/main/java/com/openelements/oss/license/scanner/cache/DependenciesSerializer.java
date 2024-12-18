package com.openelements.oss.license.scanner.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.DependencyType;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

public class DependenciesSerializer implements Serializer<Set<Dependency>> {

    @Override
    public void serialize(@NotNull DataOutput2 output, @NotNull Set<Dependency> dependencies)
            throws IOException {
        final JsonArray jsonArray = new JsonArray();
        for (Dependency dependency : dependencies) {
            final JsonObject jsonObject = new JsonObject();
            final Identifier identifier = dependency.identifier();
            final JsonObject identifierJson = new JsonObject();
            identifierJson.addProperty("name", identifier.name());
            identifierJson.addProperty("version", identifier.version());
            jsonObject.add("identifier", identifierJson);
            final License license = dependency.license();
            final JsonObject licenseJson = new JsonObject();
            licenseJson.addProperty("name", license.name());
            licenseJson.addProperty("url", license.url());
            licenseJson.addProperty("source", license.source());
            jsonObject.add("license", licenseJson);
            jsonObject.addProperty("repository", dependency.repository());
            jsonObject.addProperty("dependencyType", dependency.dependencyType().name());
            jsonArray.add(jsonObject);
        }
        output.writeUTF(jsonArray.toString());
    }

    @Override
    public Set<Dependency> deserialize(@NotNull DataInput2 input, int i) throws IOException {
        final Set<Dependency> result = new HashSet<>();
        final String string = input.readUTF();
        final JsonArray jsonArray = JsonParser.parseString(string).getAsJsonArray();
        for (int j = 0; j < jsonArray.size(); j++) {
            final JsonObject jsonObject = jsonArray.get(j).getAsJsonObject();
            final JsonObject identifierJson = jsonObject.getAsJsonObject("identifier");
            final Identifier identifier = new Identifier(identifierJson.get("name").getAsString(),
                    identifierJson.get("version").getAsString());
            final JsonObject licenseJson = jsonObject.getAsJsonObject("license");
            final License license = new License(licenseJson.get("name").getAsString(),
                    licenseJson.get("url").getAsString(), licenseJson.get("source").getAsString());
            final DependencyType dependencyType;
            if (jsonObject.has("dependencyType")) {
                dependencyType = DependencyType.valueOf(jsonObject.get("dependencyType").getAsString());
            } else {
                dependencyType = DependencyType.UNKNOWN;
            }
            result.add(new Dependency(identifier, license, jsonObject.get("repository").getAsString(),
                    dependencyType));
        }
        return result;
    }
}
