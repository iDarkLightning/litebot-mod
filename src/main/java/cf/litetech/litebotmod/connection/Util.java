package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

public class Util {
    public static String signToken(String action) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(LiteBotMod.config.secretKey);
            return JWT.create()
                    .withClaim("server_name", LiteBotMod.config.serverName)
                    .withClaim("action", action)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            return "";
        }
    }
}
