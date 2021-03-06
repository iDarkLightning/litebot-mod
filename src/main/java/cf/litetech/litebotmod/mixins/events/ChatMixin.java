package cf.litetech.litebotmod.mixins.events;

import cf.litetech.litebotmod.LiteBotMod;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ChatMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onGameMessage", at = @At(value = "RETURN", target = "net/minecraft/network/NetworkThreadUtils.forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String chatMessage = StringUtils.normalizeSpace(packet.getChatMessage());

        if (!chatMessage.startsWith("/")) {
            LiteBotMod.getDispatcher().onMessage(this.player, chatMessage);
        }
    }

    @Inject(method = "onDisconnected", at = @At(value = "RETURN"))
    public void onDisconnected(Text reason, CallbackInfo ci) {
        String message = (new TranslatableText("multiplayer.player.left", this.player.getDisplayName()))
                .getString();

        LiteBotMod.getDispatcher().onMessage(this.player, message);
        LiteBotMod.getDispatcher().onPlayerLeave(this.player);
    }
}