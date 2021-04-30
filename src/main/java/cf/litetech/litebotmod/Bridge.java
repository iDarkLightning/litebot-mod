package cf.litetech.litebotmod;

import cf.litetech.litebotmod.connection.EventActions;
import cf.litetech.litebotmod.connection.RequestData;
import cf.litetech.litebotmod.connection.ResponseData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static cf.litetech.litebotmod.connection.RequestData.signToken;


public class Bridge {
    private final String FETCH_ROUTE = "http://" + LiteBotMod.config.litebotAddress + "/server/fetch";

    public void receiveMessage(ResponseData.MessageResponse res) {
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

    public void sendMessage(String playerName, String chatMessage, String playerUUID) {
        RequestData req;

        if (playerName == null) {
            req = new RequestData(EventActions.EVENT, EventActions.Events.ON_MESSAGE, chatMessage, playerUUID);
        } else {
            req = new RequestData(EventActions.EVENT, EventActions.Events.ON_MESSAGE, chatMessage, playerUUID, playerName);
        }

        sendAction(req);
    }

    public void sendCommand(String commandName, String playerUUID, List<String> args) {
        RequestData req = new RequestData(EventActions.COMMAND, commandName, args, playerUUID);
        sendAction(req);
    }

    public ArrayList<String> fetchSuggestions(String commandName, String playerUUID, String curArg, Collection<String> args) {
        RequestData req = new RequestData(EventActions.SUGGESTER, commandName, curArg, args, playerUUID);

        String res;
        try {
            res = getReq("suggester", new Gson().toJson(req));
        } catch (IOException ignored) {
            return new ArrayList<>();
        }

        String jsonString = res.substring(res.indexOf('['), res.lastIndexOf(']'));
        if (jsonString.isEmpty()) return new ArrayList<>();

        Type type = new TypeToken<List<String>>(){}.getType();
        return new Gson().fromJson(jsonString + "]", type);
    }

    private void sendAction(RequestData req) {
        LiteBotMod.getConnection().send(new Gson().toJson(req));
    }

    private String getReq(String item, String data) throws IOException {
        URL url = new URL(FETCH_ROUTE + "/" + item + "?data=" + data);
        URLConnection con = url.openConnection();
        HttpURLConnection http  = (HttpURLConnection) con;


        http.setRequestMethod("GET");
        http.setRequestProperty("Authorization", "Bearer " + signToken(EventActions.SUGGESTER.val));
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        http.getInputStream()));

        StringBuilder res = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            res.append(inputLine);
        in.close();

        return res.toString();
    }
}
