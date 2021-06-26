package cf.litetech.litebotmod.connection.rpc;

import cf.litetech.litebotmod.LiteBotMod;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

import java.util.UUID;

public class MessageHandler extends RPCHandler<MessageHandler, MessageHandler.MessageHandlerDeserializer> {
    public MessageHandler(String name, Class<MessageHandlerDeserializer> deserializerClass) {
        super(name, deserializerClass);
    }

    public static class MessageHandlerDeserializer {
        public String message;
        public String player;
        public boolean opOnly;
    }

    @Override
    public void handle(MessageHandlerDeserializer args) {
        Text message = Text.Serializer.fromJson(args.message);

        if ((!args.opOnly) && args.player == null) {
            LiteBotMod.getServer().getPlayerManager().broadcastChatMessage(
                    message,
                    MessageType.CHAT,
                    Util.NIL_UUID
            );

        } else if (args.opOnly && LiteBotMod.getServer().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            for (ServerPlayerEntity serverPlayerEntity : LiteBotMod.getServer().getPlayerManager().getPlayerList()) {
                if (LiteBotMod.getServer().getPlayerManager().isOperator(serverPlayerEntity.getGameProfile())) {
                    serverPlayerEntity.sendSystemMessage(message, Util.NIL_UUID);
                }
            }
        } else if (args.player != null) {
            ServerPlayerEntity player = LiteBotMod.getServer().getPlayerManager().
                    getPlayer(UUID.fromString(args.player));

            if (player != null) {
                player.sendSystemMessage(message, Util.NIL_UUID);
            }
        }
    }
}