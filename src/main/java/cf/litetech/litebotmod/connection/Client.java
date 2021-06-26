package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotExtension;
import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.rpc.RPCHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.function.Function;

public class Client extends WebSocketClient {
    public Client(URI serverUri) {
        super(serverUri);
    }
    public HashMap<String, Function<String, ? extends Class<Void>>> ongoingCallbacks = new HashMap<>();

    @Override
    public void onOpen(ServerHandshake handShakeData) {
        RequestData data = new RequestData(EventActions.AUTH);
        send(new Gson().toJson(data));

        LiteBotMod.getExtensions().forEach(LiteBotExtension::onWebsocketOpen);
    }

    @Override
    public void onMessage(String message) {
        JsonObject data = new JsonParser().parse(message).getAsJsonObject();
        if (data.get("name") != null && RPCHandler.getRegisteredHandlers().containsKey(data.get("name").getAsString())) {
            RPCHandler.getRegisteredHandlers()
                    .get(data.get("name").getAsString())
                    .handle(RPCHandler.getRegisteredHandlers()
                            .get(data.get("name").getAsString())
                            .deserializeArgs(data.get("data")));
        } else if (data.get("id") != null && ongoingCallbacks.containsKey(data.get("id").getAsString())) {
            ongoingCallbacks.get(data.get("id").getAsString()).apply(data.get("res").toString());
        }

        LiteBotMod.getExtensions().forEach(e -> e.onWebsocketMessage(message));
    }

    public void send(String message) {
        if (this.isOpen()) {
            super.send(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LiteBotMod.LOGGER.error(reason);

        LiteBotMod.getExtensions().forEach(e -> e.onWebsocketClose(code, reason, remote));
    }

    @Override
    public void onError(Exception ex) {
        LiteBotMod.getExtensions().forEach(e -> e.onWebsocketError(ex));
    }
}
