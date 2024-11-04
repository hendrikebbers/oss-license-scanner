package com.openelements.oss.license.scanner.licenses;

import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LicenseCache {

    private final Map<Identifier, License> licenses = new ConcurrentHashMap<>();

    private final static LicenseCache INSTANCE = new LicenseCache();

    private LicenseCache() {
    }

    public void addLicense(Identifier identifier, License license) {
        licenses.put(identifier, license);
    }

    public boolean contains(Identifier identifier) {
        return licenses.containsKey(identifier);
    }

    public License getLicense(Identifier identifier) {
        return licenses.get(identifier);
    }

    public License computeIfAbsent(Identifier identifier, Supplier<License> supplier) {
        return licenses.computeIfAbsent(identifier, i -> supplier.get());
    }

    public static LicenseCache getInstance() {
        return INSTANCE;
    }

}
