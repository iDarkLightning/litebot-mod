package cf.litetech.litebotmod.connection.rpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;

@SuppressWarnings("rawtypes")
public abstract class RPCHandler<T extends RPCHandler, R> {
    public String name;
    private final Class<R> deserializerClass;
    private static final HashMap<String, RPCHandler<? extends RPCHandler, Object>> REGISTERED_HANDLERS = new HashMap<>();

    public static HashMap<String, RPCHandler<? extends RPCHandler, Object>> getRegisteredHandlers() {
        return REGISTERED_HANDLERS;
    }

    @SuppressWarnings("unchecked")
    public RPCHandler(String name, Class<R> deserializerClass) {
        REGISTERED_HANDLERS.put(name, (T)this);
        this.deserializerClass = deserializerClass;
    }

    public R deserializeArgs(JsonElement jsonString) {
        return new Gson().fromJson(jsonString, deserializerClass);
    }

    public abstract void handle(R args);

}
