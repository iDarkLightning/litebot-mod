package cf.litetech.litebotmod;

public interface LiteBotExtension {

    default void onWebsocketOpen() {}

    default void onWebsocketMessage(String message) {}

    default void onWebsocketClose(int code, String reason, boolean remote) {}

    default void onWebsocketError(Exception ex) {}

    void registerHooks();

    void registerRPC();
}
