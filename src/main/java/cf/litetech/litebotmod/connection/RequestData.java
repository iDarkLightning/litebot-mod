package cf.litetech.litebotmod.connection;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestData {
    private String auth;
    private  String player;
    private String name;
    private String arg;
    private List<?> args;

    RequestData(EventActions action) {
        this.auth = signToken(action.val);
    }

    public RequestData(EventActions action, EventActions.Events eventName, Object... args) {
        this.auth = signToken(action.val);
        this.name = eventName.val;
        this.args = new ArrayList<>(Arrays.asList(args));
    }

    public RequestData(EventActions action, String commandName, List<String> args, String playerUUID) {
        this.auth = signToken(action.val);
        this.name = commandName;
        this.args = args;
        this.player = playerUUID;
    }

    public RequestData(String commandName, String arg, List<String> args, String playerUUID) {
        this.name = commandName;
        this.arg = arg;
        this.args = args;
        this.player = playerUUID;
    }

    public static String signToken(String action) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("rabbitsarecool");
            return JWT.create()
                    .withClaim("server_name", "smp")
                    .withClaim("action", action)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            return "";
        }
    }
}
