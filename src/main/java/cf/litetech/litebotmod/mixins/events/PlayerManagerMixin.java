package cf.litetech.litebotmod.mixins.events;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.EventsDispatcher;
import cf.litetech.litebotmod.network.ServerNetworkHandler;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(at = @At("RETURN"), method = "onPlayerConnect")
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        String message;
        GameProfile gameProfile = player.getGameProfile();
        UserCache userCache = this.server.getUserCache();
        GameProfile gameProfile2 = userCache.getByUuid(gameProfile.getId());
        String string = gameProfile2 == null ? gameProfile.getName() : gameProfile2.getName();

        if (player.getGameProfile().getName().equalsIgnoreCase(string)) {
            message = new TranslatableText("multiplayer.player.joined", new Object[]{player.getDisplayName()}).getString();
        } else {
            message = new TranslatableText("multiplayer.player.joined.renamed", new Object[]{player.getDisplayName(), string}).getString();
        }

        LiteBotMod.getDispatcher().onMessage(player, message);
        LiteBotMod.getDispatcher().onPlayerJoin(player);
        ServerNetworkHandler.onPlayerJoin(player);
    }
}