package com.openelements.oss.license.scanner.clients;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.api.ApiConstants;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CratesClient {

    private final static Logger log = LoggerFactory.getLogger(CratesClient.class);

    private static boolean compareVersions(String v1, String v2) {
        if(Objects.equals(v1, v2)) {
            return true;
        }
        if(v1 != null && v1.startsWith("v")) {
            if(Objects.equals(v1.substring(1), v2)) {
                return true;
            }
        }
        if(v2 != null && v2.startsWith("v")) {
            if(Objects.equals(v2.substring(1), v1)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<License> getLicenceForCrate(Identifier identifier) {
        try {
            log.info("Getting license from crates.io for {}", identifier);
            final String url = "https://crates.io/api/v1/crates/" + identifier.name();
            final HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "https://github.com/hendrikebbers/oss-license-scanner");
            HttpRequest request = builder.build();
            final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get licence info for " + identifier.name() + ":" + identifier.version() + ": status code " + response.statusCode());
            }
            String body = response.body();
            final JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            return root.getAsJsonArray("versions").asList().stream()
                            .filter(elem -> {
                                final String version = elem.getAsJsonObject().get("num").getAsString();
                                return compareVersions(version, identifier.version());
                            }).map(elem -> elem.getAsJsonObject().get("license").getAsString())
                            .map(name -> new License(name, ApiConstants.UNKNOWN, url))
                    .findFirst();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get licence info for " + identifier.name() + ":" + identifier.version(), e);
        }
    }
}
