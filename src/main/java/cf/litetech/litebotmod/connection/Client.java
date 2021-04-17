package cf.litetech.litebotmod.connection;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class Client extends WebSocketClient {
    public Client(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        RequestData data = new RequestData(signToken());
        send(new Gson().toJson(data));
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received messages" + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println(reason);
    }

    @Override
    public void onError(Exception ex) {

    }

    private String signToken() {
        try {
            Algorithm algorithm = Algorithm.HMAC256("rabbitsarecool");
            return JWT.create()
                    .withClaim("server", "smp")
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            return "";
        }
    }
}
