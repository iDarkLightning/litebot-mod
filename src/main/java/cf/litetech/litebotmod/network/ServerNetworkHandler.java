package cf.litetech.litebotmod.network;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.RequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class ServerNetworkHandler {
    public static HashSet<ServerPlayerEntity> validPlayers = new HashSet<>();


    public static void handleData(PacketByteBuf packet, ServerPlayerEntity player) {
        if (packet == null) return;
        int id = packet.readVarInt();
        if (id == LiteBotClient.HELLO) onHello(packet, player);
        if (id == LiteBotClient.DATA) onData(packet, player);
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        if (!player.networkHandler.connection.isLocal()) {
            long i = LiteBotMod.getConnection().isOpen() ? 1 : 0;

            player.networkHandler.sendPacket(new CustomPayloadS2CPacket(LiteBotClient.LITEBOT_CHANNEL,
                    new PacketByteBuf(Unpooled.buffer()).writeVarInt(LiteBotClient.HI).writeVarLong(i)));
        } else validPlayers.add(player);
    }

    private static void onHello(PacketByteBuf packet, ServerPlayerEntity player) {
        validPlayers.add(player);
        LiteBotMod.LOGGER.info(player.getDisplayName().getString() + " joined the game on a litebot client!");
    }

    private static void onData(PacketByteBuf packet, ServerPlayerEntity player) {
        JsonObject data = new JsonParser().parse(packet.readString(packet.readableBytes())).getAsJsonObject();
        RequestBuilder<Object> req = new RequestBuilder<>(data.get("action").getAsString())
                .setName(data.get("name").getAsString());

        if (data.get("player") != null) {
            req.setPlayer(
                    LiteBotMod.getServer().getPlayerManager().getPlayer(
                            UUID.fromString(data.get("player").getAsJsonObject().get("uuid").getAsString()))
            );
        }

        for (Map.Entry<String, JsonElement> key : data.get("args").getAsJsonObject().entrySet()) {
            req.addArg(key.getKey(), data.get(key.getKey()).getAsJsonObject());
        }

        HashMap<String, Object> res = new HashMap<>();
        res.put("id", data.get("auth").getAsString());
        res.put("res", req.makeRequest());

        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(LiteBotClient.LITEBOT_CHANNEL,
                new PacketByteBuf(Unpooled.buffer()).writeVarInt(LiteBotClient.DATA).writeString(new Gson().toJson(res))));
    }
}
