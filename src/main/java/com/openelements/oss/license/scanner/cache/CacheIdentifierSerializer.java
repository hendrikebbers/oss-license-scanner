package com.openelements.oss.license.scanner.cache;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openelements.oss.license.scanner.api.Identifier;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

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
