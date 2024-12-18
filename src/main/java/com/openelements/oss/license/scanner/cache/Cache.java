package com.openelements.oss.license.scanner.cache;

import com.openelements.oss.license.scanner.api.Dependency;
import com.openelements.oss.license.scanner.api.Identifier;
import java.util.Map;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.DBMaker;
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

    public Set<Dependency> getGo(Identifier cacheIdentifier) {
        return get("Go", cacheIdentifier);
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

    public boolean containsKeyForGo(Identifier identifier) {
        return containsKey("Go", identifier);
    }

    public boolean containsKey(String language, Identifier identifier) {
        return containsKey(new CacheIdentifier(language, identifier));
    }

    public void putGo(Identifier identifier, Set<Dependency> dependencies) {
        put("Go", identifier, dependencies);
    }

    public boolean containsKeyForJava(Identifier identifier) {
        return containsKey("Java", identifier);
    }

    public Set<Dependency> getJava(Identifier identifier) {
        return get("Java", identifier);
    }

    public void putJava(Identifier identifier, Set<Dependency> dependencies) {
        put("Java", identifier, dependencies);
    }

    public boolean containsKeyForSwift(Identifier identifier) {
        return containsKey("Swift", identifier);
    }

    public Set<Dependency> getSwift(Identifier identifier) {
        return get("Swift", identifier);
    }

    public void putSwift(Identifier identifier, Set<Dependency> dependencies) {
        put("Swift", identifier, dependencies);
    }

}
