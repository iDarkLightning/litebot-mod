package cf.litetech.litebotmod.commands;

import com.google.gson.Gson;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

public class Serializers {

    public static String serializePlayer(ServerPlayerEntity player) {
        HashMap<String, Object> playerData = new HashMap<>();

        playerData.put("uuid", player.getUuidAsString());
        playerData.put("pos_x", player.getBlockPos().getX());
        playerData.put("pos_y", player.getBlockPos().getY());
        playerData.put("pos_z", player.getBlockPos().getZ());

        return new Gson().toJson(playerData);
    }
}
