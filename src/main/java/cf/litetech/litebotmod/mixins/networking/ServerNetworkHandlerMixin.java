package cf.litetech.litebotmod.mixins.networking;

import cf.litetech.litebotmod.helpers.CustomPayloadC2SPacketHelper;
import cf.litetech.litebotmod.network.LiteBotClient;
import cf.litetech.litebotmod.network.ServerNetworkHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onCustomLiteBotPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        Identifier channel = ((CustomPayloadC2SPacketHelper) packet).getPacketChannel();
        if (LiteBotClient.LITEBOT_CHANNEL.equals(channel)) {
            ServerNetworkHandler.handleData(((CustomPayloadC2SPacketHelper) packet).getPacketData(), player);
            ci.cancel();
        }
    }
}
