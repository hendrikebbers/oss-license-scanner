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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PypiClient {

    private final static Logger log = LoggerFactory.getLogger(PypiClient.class);

    public static Optional<License> getLicense(Identifier identifier) {
        try {
            log.info("Getting license from pypi.org for {}", identifier);
            final String url = "https://pypi.org/pypi/" + identifier.name() + "/" + identifier.version() + "/json";
            final HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "https://github.com/hendrikebbers/oss-license-scanner");
            HttpRequest request = builder.build();
            final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Failed to get licence info for " + identifier.name() + ":" + identifier.version()
                                + ": status code " + response.statusCode());
            }
            String body = response.body();
            final JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            final JsonObject infoObject = root.get("info").getAsJsonObject();
            if (!infoObject.has("license")) {
                return Optional.empty();
            }
            final JsonElement jsonElement = infoObject.get("license");
            if (jsonElement.isJsonNull()) {
                log.info("License not provided at pypi.org for {}", identifier);
                return Optional.empty();
            }
            final String license = jsonElement.getAsString();
            return Optional.ofNullable(new License(license, ApiConstants.UNKNOWN, url));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get licence info for " + identifier.name() + ":" + identifier.version(), e);
        }
    }

    public static Optional<String> getProjectUrl(Identifier identifier) {
        try {
            log.info("Getting license from pypi.org for {}", identifier);
            final String url = "https://pypi.org/pypi/" + identifier.name() + "/" + identifier.version() + "/json";
            final HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "https://github.com/hendrikebbers/oss-license-scanner");
            HttpRequest request = builder.build();
            final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Failed to get licence info for " + identifier.name() + ":" + identifier.version()
                                + ": status code " + response.statusCode());
            }
            String body = response.body();
            final JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            final JsonObject infoObject = root.get("info").getAsJsonObject();
            if (!infoObject.has("project_urls")) {
                log.info("Repository url not provided at pypi.org for {}", identifier);
                return Optional.empty();
            }
            final JsonObject urlsObject = infoObject.get("project_urls").getAsJsonObject();
            return urlsObject.asMap().values().stream()
                    .filter(JsonElement::isJsonPrimitive)
                    .map(JsonElement::getAsString)
                    .filter(value -> value.contains("github") || value.contains("sourceforge"))
                    .findFirst();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to get licence info for " + identifier.name() + ":" + identifier.version(), e);
        }
    }
}
