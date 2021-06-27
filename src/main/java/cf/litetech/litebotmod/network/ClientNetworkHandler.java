package cf.litetech.litebotmod.network;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

import java.util.HashMap;
import java.util.function.Function;

public class ClientNetworkHandler {
    public static HashMap<String, Function<String, ? extends Class<Void>>> ongoingCallbacks = new HashMap<>();

    public static void handleData(PacketByteBuf packet, ClientPlayerEntity player) {
        if (packet == null) return;
        int id = packet.readVarInt();
        if (id == LiteBotClient.HI) onHi(packet);
        if (id == LiteBotClient.DATA) onData(packet, player);
        if (id == LiteBotClient.LITEBOT_CONNECT) onLiteBotConnect(packet);
    }

    public static void respondHello() {
        LiteBotClient.getClientPlayer().networkHandler.sendPacket(new CustomPayloadC2SPacket(
                LiteBotClient.LITEBOT_CHANNEL,
                new PacketByteBuf(Unpooled.buffer()).writeVarInt(LiteBotClient.HELLO)
        ));
    }

    private static void onHi(PacketByteBuf packet) {
        LiteBotClient.setLitebotServer();
        LiteBotClient.setLitebotConnected(packet.readVarLong() == 1);
        if (LiteBotClient.getClientPlayer() != null) respondHello();
    }

    private static void onLiteBotConnect(PacketByteBuf packet) {
        LiteBotClient.setLitebotConnected(packet.readVarLong() == 1);
    }

    private static void onData(PacketByteBuf packet, ClientPlayerEntity player) {
        JsonObject data = new JsonParser().parse(packet.readString()).getAsJsonObject();
        if (data.get("id") != null && ongoingCallbacks.containsKey(data.get("id").getAsString())) {
            ongoingCallbacks.get(data.get("id").getAsString()).apply(data.get("res").toString());
            ongoingCallbacks.remove(data.get("id").getAsString());
        }
    }
}
