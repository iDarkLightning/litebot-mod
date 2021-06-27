package cf.litetech.litebotmod.helpers;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface CustomPayloadC2SPacketHelper {
    Identifier getPacketChannel();
    PacketByteBuf getPacketData();
}
