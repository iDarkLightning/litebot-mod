package cf.litetech.litebotmod.mixins.networking;

import cf.litetech.litebotmod.helpers.CustomPayloadC2SPacketHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin implements CustomPayloadC2SPacketHelper {

    @Shadow private Identifier channel;

    @Shadow private PacketByteBuf data;

    @Override
    public Identifier getPacketChannel() {
        return channel;
    }

    @Override
    public PacketByteBuf getPacketData() {
        return new PacketByteBuf(this.data.copy());
    }
}
