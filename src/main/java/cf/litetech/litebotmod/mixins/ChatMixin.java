package cf.litetech.litebotmod.mixins;

import cf.litetech.litebotmod.Bridge;
import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.RequestData;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ChatMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onGameMessage", at = @At(value = "RETURN", target = "net/minecraft/network/NetworkThreadUtils.forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String chatMessage = StringUtils.normalizeSpace(packet.getChatMessage());
        String playerName = this.player.getDisplayName().getString();

        if (!chatMessage.startsWith("/")) {
            LiteBotMod.getBridge().sendMessage(playerName, chatMessage);
        }
    }

    @Inject(method = "onDisconnected", at = @At(value = "RETURN"))
    public void onDisconnected(Text reason, CallbackInfo ci) {
        String message = (new TranslatableText("multiplayer.player.left", this.player.getDisplayName()))
                .getString();

        LiteBotMod.getBridge().sendMessage(null, message);
    }
}