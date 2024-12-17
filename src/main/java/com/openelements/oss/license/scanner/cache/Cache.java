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
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache {

    private final static Logger log = LoggerFactory.getLogger(Cache.class);

    private final static Cache instance = new Cache();

    private DB database;

    private final Map<CacheIdentifier, Set<Dependency>> dependencies;

    private Cache() {
        database = DBMaker.fileDB("dependencies.db").make();
        dependencies = database.hashMap("dependencies", new CacheIdentifierSerializer(), new DependenciesSerializer())
                .createOrOpen();
        Runtime.getRuntime().addShutdownHook(new Thread(database::close));
    }

    public void put(CacheIdentifier cacheIdentifier, Set<Dependency> dependencies) {
        if (this.dependencies.containsKey(cacheIdentifier)) {
            final Set<Dependency> currentDependencies = this.dependencies.get(cacheIdentifier);
            if (currentDependencies.size() == dependencies.size() && currentDependencies.containsAll(dependencies)) {
                log.warn("Attempted to add same dependency for an identifier that is already in cache: {}",
                        cacheIdentifier);
                return;
            } else {
                log.warn("Attempted to add different dependency for an identifier that is already in cache: {}",
                        cacheIdentifier);
            }
        }
        this.dependencies.put(cacheIdentifier, dependencies);
        database.commit();
    }

    public Set<Dependency> get(CacheIdentifier cacheIdentifier) {
        return dependencies.get(cacheIdentifier);
    }


    public boolean containsKey(CacheIdentifier identifier) {
        return dependencies.containsKey(identifier);
    }

    public static Cache getInstance() {
        return instance;
    }

    public void putJS(Identifier identifier, Set<Dependency> dependencies) {
        this.put("JS", identifier, dependencies);
    }

    public void put(String language, Identifier identifier, Set<Dependency> dependencies) {
        this.put(new CacheIdentifier(language, identifier), dependencies);
    }

    public Set<Dependency> getJS(Identifier cacheIdentifier) {
        return get("JS", cacheIdentifier);
    }

    public Set<Dependency> get(String language, Identifier cacheIdentifier) {
        return get(new CacheIdentifier(language, cacheIdentifier));
    }

    public boolean containsKeyForJS(Identifier identifier) {
        return containsKey("JS", identifier);
    }

    public boolean containsKey(String language, Identifier identifier) {
        return containsKey(new CacheIdentifier(language, identifier));
    }

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

    public class CacheIdentifierSerializer implements Serializer<CacheIdentifier> {

        @Override
        public void serialize(@NotNull DataOutput2 dataOutput2, @NotNull CacheIdentifier cacheIdentifier)
                throws IOException {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("language", cacheIdentifier.language());
            final JsonObject identifier = new JsonObject();
            identifier.addProperty("name", cacheIdentifier.identifier().name());
            identifier.addProperty("version", cacheIdentifier.identifier().version());
            jsonObject.add("identifier", identifier);
            dataOutput2.writeUTF(jsonObject.toString());
        }

        @Override
        public CacheIdentifier deserialize(@NotNull DataInput2 input, int i) throws IOException {
            final String string = input.readUTF();
            final JsonObject jsonObject = JsonParser.parseString(string).getAsJsonObject();
            Identifier identifier = new Identifier(jsonObject.getAsJsonObject("identifier").get("name").getAsString(),
                    jsonObject.getAsJsonObject("identifier").get("version").getAsString());
            return new CacheIdentifier(jsonObject.get("language").getAsString(), identifier);
        }
    }
}
