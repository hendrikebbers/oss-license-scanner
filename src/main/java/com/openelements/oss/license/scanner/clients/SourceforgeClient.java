package com.openelements.oss.license.scanner.clients;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.api.Identifier;
import com.openelements.oss.license.scanner.api.License;
import com.openelements.oss.license.scanner.tools.PythonTool;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceforgeClient {

    private final static Logger log = LoggerFactory.getLogger(SourceforgeClient.class);

    public static Optional<License> call(String project) {
        try {
            log.info("Getting license from sourceforge.net for {}", project);
            final String url = "https://sourceforge.net/rest/p/" + project;
            final HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "https://github.com/hendrikebbers/oss-license-scanner");
            HttpRequest request = builder.build();
            final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get licence info for " + project + ": status code " + response.statusCode());
            }
            String body = response.body();
            final JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            if(root.has("categories")) {
                final JsonObject categories = root.getAsJsonObject("categories");
                if(categories.has("license")) {
                    final String license = categories.get("license").getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsJsonObject)
                            .map(obj -> obj.get("fullname").getAsString())
                            .reduce("", (a, b) -> a +","+ b);
                    if(!license.isBlank()) {
                        return Optional.of(new License(license, "UNKNOWN", url));
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get licence info for " + project, e);
        }
    }
}
