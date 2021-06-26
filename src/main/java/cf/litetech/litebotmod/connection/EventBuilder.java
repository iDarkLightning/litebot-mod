package cf.litetech.litebotmod.connection;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.commands.Serializers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

import static cf.litetech.litebotmod.connection.Util.signToken;

public class EventBuilder {
    public enum Events {
        AUTH("auth"),
        EVENT("event"),
        COMMAND("command");

        public String val;

        Events(String val) {
            this.val = val;
        }
    }

    private String auth;
    private String name;
    private String player;
    private HashMap<String, Object> args = new HashMap<>();

    public EventBuilder(Events event) {
        this.auth = signToken(event.val);
    }

    public EventBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public EventBuilder setPlayer(ServerPlayerEntity player) {
        this.player = Serializers.serializePlayer(player);
        return this;
    }

    public EventBuilder setArgs(HashMap<String, Object> args) {
        this.args = args;
        return this;
    }

    public EventBuilder addArg(String string, Object object) {
        this.args.put(string, object);
        return this;
    }

    public int dispatchEvent() {
        LiteBotMod.getConnection().send(new Gson().toJson(this));
        return 0;
    }
}
