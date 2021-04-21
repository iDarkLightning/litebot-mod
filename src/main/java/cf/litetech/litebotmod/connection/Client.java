package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.commands.CommandRegisters;
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
    }

    @Override
    public void onMessage(String message) {
        ResponseData data = new Gson().fromJson(message, ResponseData.class);
        if (data.messageData != null) {
            LiteBotMod.getBridge().receiveMessage(data.messageData);
        } else if (data.commandData != null) {
            CommandRegisters.setCommandData(data.commandData);
        }
    }

    public void send(String message) {
        if (this.isOpen()) {
            super.send(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(reason);
    }

    @Override
    public void onError(Exception ex) {

    }
}
