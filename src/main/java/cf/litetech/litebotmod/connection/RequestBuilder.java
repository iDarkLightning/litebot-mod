package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.commands.Serializers;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RequestBuilder<T> {
    public String auth;
    private String name;
    private String player;
    private HashMap<String, Object> args = new HashMap<>();
    public String built;

    public RequestBuilder(String action) {
        this.auth = this.signToken(action);
    }

    public RequestBuilder<T> setName(String name) {
        this.name = name;
        return this;
    }

    public RequestBuilder<T> setPlayer(ServerPlayerEntity player) {
        this.player = Serializers.serializePlayer(player);
        return this;
    }

    public RequestBuilder<T> setArgs(HashMap<String, Object> args) {
        this.args = args;
        return this;
    }

    public RequestBuilder<T> addArg(String string, Object object) {
        this.args.put(string, object);
        return this;
    }

    public T makeRequest() {
        LiteBotMod.getConnection().send(new Gson().toJson(this));

        final AtomicReference<T> result = new AtomicReference<>();

        final FutureTask<String> futureTask = new FutureTask<>(() -> {}, "");
        final Function<String, ? extends Class<Void>> callback = (String res) -> {
            result.set(new Gson().fromJson(res, new TypeToken<T>(){}.getType()));
            futureTask.run();
            return Void.TYPE;
        };

        LiteBotMod.getConnection().ongoingCallbacks.put(this.auth, callback);
        try {
            futureTask.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            return new AtomicReference<T>().get();
        } catch (Exception ignored) {}

        return result.get();
    }

    private String signToken(String action) {
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
