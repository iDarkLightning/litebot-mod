package cf.litetech.litebotmod;

import cf.litetech.litebotmod.connection.EventActions;
import cf.litetech.litebotmod.connection.RequestData;
import cf.litetech.litebotmod.connection.ResponseData;
import com.google.gson.Gson;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

import java.util.UUID;


public class Bridge {
    private void sendAction(RequestData req) {
        LiteBotMod.getConnection().send(new Gson().toJson(req));
    }

    public void receiveMessage(ResponseData res) {
        Text message = Text.Serializer.fromJson(res.message);


        if ((!res.opOnly) && res.player == null) {
            LiteBotMod.getServer().getPlayerManager().broadcastChatMessage(
                    message,
                    MessageType.CHAT,
                    Util.NIL_UUID
            );

        } else if (res.opOnly && LiteBotMod.getServer().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
            for (ServerPlayerEntity serverPlayerEntity : LiteBotMod.getServer().getPlayerManager().getPlayerList()) {
                if (LiteBotMod.getServer().getPlayerManager().isOperator(serverPlayerEntity.getGameProfile())) {
                    serverPlayerEntity.sendSystemMessage(message, Util.NIL_UUID);
                }
            }
        } else if (res.player != null) {
            ServerPlayerEntity player = LiteBotMod.getServer().getPlayerManager().
                    getPlayer(UUID.fromString(res.player));

            if (player != null) {
                player.sendSystemMessage(message, Util.NIL_UUID);
            }
        }

    }

    public void sendMessage(String playerName, String chatMessage) {
        RequestData req;

        if (playerName == null) {
            req = new RequestData(EventActions.EVENT, EventActions.Events.ON_MESSAGE, chatMessage);
        } else {
            req = new RequestData(EventActions.EVENT, EventActions.Events.ON_MESSAGE, chatMessage, playerName);
        }

        sendAction(req);
    }
}
