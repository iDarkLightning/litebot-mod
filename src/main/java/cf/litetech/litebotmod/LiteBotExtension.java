package cf.litetech.litebotmod;

public interface LiteBotExtension {

    default void onWebsocketOpen() {}

    default void onWebsocketMessage(String message) {}

    default void onWebsocketClose() {}

    default void onWebsocketError() {}

    void registerHooks();

    static void register() {};
}
