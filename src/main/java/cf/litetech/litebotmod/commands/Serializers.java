package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import com.google.gson.Gson;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;

public class Serializers {

    public static String serializePlayer(ServerPlayerEntity player) {
        HashMap<String, Object> playerData = new HashMap<>();

        playerData.put("uuid", player.getUuidAsString());
        playerData.put("dimension", player.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).
                getId(player.getEntityWorld().getDimension()).toString());
        playerData.put("pos_x", player.getBlockPos().getX());
        playerData.put("pos_y", player.getBlockPos().getY());
        playerData.put("pos_z", player.getBlockPos().getZ());

        return new Gson().toJson(playerData);
    }

    public static String serializeBlockPos(BlockPos pos) {
        ArrayList<Integer> posData = new ArrayList<>();

        posData.add(pos.getX());
        posData.add(pos.getY());
        posData.add(pos.getZ());

        return new Gson().toJson(posData);
    }

    public static String serializeDimension(ServerWorld world) {
        return LiteBotMod.getServer().getRegistryManager().get(Registry.DIMENSION_TYPE_KEY)
                .getId(world.getDimension()).toString();
    }
}
