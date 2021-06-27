package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Util {
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    public static String signToken(String action) {
        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(new JsonParser().parse(JWT_HEADER).toString().getBytes(StandardCharsets.UTF_8));
        JsonObject payload = new JsonObject();
        payload.addProperty("server_name", LiteBotMod.config.serverName);
        payload.addProperty("action", action);
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));

        String data = encodedHeader + "." + encodedPayload;

        byte[] hash = LiteBotMod.config.secretKey.getBytes(StandardCharsets.UTF_8);
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKeySpec);

            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return data + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ignored) {
            return null;
        }
    }

}
