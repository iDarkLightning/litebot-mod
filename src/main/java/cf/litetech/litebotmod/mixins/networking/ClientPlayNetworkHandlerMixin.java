package cf.litetech.litebotmod.mixins.networking;

import cf.litetech.litebotmod.network.ClientNetworkHandler;
import cf.litetech.litebotmod.network.LiteBotClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow private MinecraftClient client;

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (LiteBotClient.LITEBOT_CHANNEL.equals(packet.getChannel())) {
            ClientNetworkHandler.handleData(packet.getData(), this.client.player);
            ci.cancel();
        }
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void onGameJoined(GameJoinS2CPacket packet, CallbackInfo ci) {
        LiteBotClient.onGameJoined(this.client.player);
    }

    @Inject(method = "onDisconnect", at = @At("RETURN"))
    private void onDisconnect(DisconnectS2CPacket packet, CallbackInfo ci) {
        LiteBotClient.disconnect();
    }
}
