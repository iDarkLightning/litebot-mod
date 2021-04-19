package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
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
        LiteBotMod.getBridge().receiveMessage(data);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(reason);
    }

    @Override
    public void onError(Exception ex) {

    }
}
