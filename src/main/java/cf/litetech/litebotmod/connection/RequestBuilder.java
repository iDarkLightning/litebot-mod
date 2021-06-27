package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.commands.Serializers;
import cf.litetech.litebotmod.network.ClientNetworkHandler;
import cf.litetech.litebotmod.network.LiteBotClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static cf.litetech.litebotmod.connection.Util.signToken;

public class RequestBuilder<T> {
    private String auth;
    private String action;
    private String name;
    private String player;
    private HashMap<String, Object> args = new HashMap<>();

    public RequestBuilder(String action) {
        this.auth = signToken(action);
        this.action = action;
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

    public T requestToServer() {
        LiteBotClient.getClientPlayer().networkHandler.sendPacket(
                new CustomPayloadC2SPacket(LiteBotClient.LITEBOT_CHANNEL,
                        new PacketByteBuf(Unpooled.buffer()).writeVarInt(LiteBotClient.DATA).writeString(new Gson().toJson(this)))
        );

        final AtomicReference<T> result = new AtomicReference<>();

        final FutureTask<String> futureTask = new FutureTask<>(() -> {}, "");
        final Function<String, ? extends Class<Void>> callback = (String res) -> {
            result.set(new Gson().fromJson(res, new TypeToken<T>(){}.getType()));
            futureTask.run();
            return Void.TYPE;
        };

        ClientNetworkHandler.ongoingCallbacks.put(this.auth, callback);
        try {
            futureTask.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException exception) {
            return new AtomicReference<T>().get();
        } catch (Exception ignored) {}

        return result.get();
    }

}
