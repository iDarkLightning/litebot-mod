package cf.litetech.litebotmod;

import cf.litetech.litebotmod.commands.Serializers;
import cf.litetech.litebotmod.connection.EventActions;
import cf.litetech.litebotmod.connection.RequestData;
import com.google.gson.Gson;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

public class Bridge {
    public void sendMessage(String playerName, String chatMessage, String playerUUID) {
        RequestData req;

        if (playerName == null) {
            req = new RequestData(EventActions.EVENT, EventActions.Events.ON_MESSAGE, chatMessage, playerUUID);
        } else {
            req = new RequestData(EventActions.EVENT, EventActions.Events.ON_MESSAGE, chatMessage, playerUUID, playerName);
        }

        sendAction(req);
    }

    public void sendCommand(String commandName, ServerPlayerEntity player, Map<String, String> args) {
        RequestData req = new RequestData(EventActions.COMMAND, commandName, args, Serializers.serializePlayer(player));
        sendAction(req);
    }

    private void sendAction(RequestData req) {
        LiteBotMod.getConnection().send(new Gson().toJson(req));
    }
}
