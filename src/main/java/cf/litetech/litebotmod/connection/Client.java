package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotExtension;
import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.commands.CommandRegisters;
import cf.litetech.litebotmod.commands.ExecutingCommand;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class Client extends WebSocketClient {
    public Client(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handShakeData) {
        RequestData data = new RequestData(EventActions.AUTH);
        send(new Gson().toJson(data));

        LiteBotMod.getExtensions().forEach(LiteBotExtension::onWebsocketOpen);
    }

    @Override
    public void onMessage(String message) {
        ResponseData data = new Gson().fromJson(message, ResponseData.class);
        if (data.messageData != null) {
            LiteBotMod.getBridge().receiveMessage(data.messageData);
        } else if (data.commandData != null) {
            CommandRegisters.setCommandData(data.commandData);
        } else if (data.afterInvoke != null) {
            ExecutingCommand.callAfterInvoke(data.afterInvoke);
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
