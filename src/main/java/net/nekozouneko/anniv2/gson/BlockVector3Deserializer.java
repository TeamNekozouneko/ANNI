package net.nekozouneko.anniv2.gson;

import com.google.gson.*;
import com.sk89q.worldedit.math.BlockVector3;

import java.lang.reflect.Type;

public class BlockVector3Deserializer implements JsonDeserializer<BlockVector3> {

    @Override
    public BlockVector3 deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return BlockVector3.at(
                obj.get("x").getAsInt(),
                obj.get("y").getAsInt(),
                obj.get("z").getAsInt()
        );
    }
}
