package cf.litetech.litebotmod.network;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public class LiteBotClient {
    public static final Identifier LITEBOT_CHANNEL = new Identifier("litebot:channel");
    public static final int HI = 0;
    public static final int HELLO = 1;
    public static final int LITEBOT_CONNECT = 2;
    public static final int DATA = 3;
    private static ClientPlayerEntity clientPlayer = null;
    private static boolean litebotServer = false;
    private static boolean litebotConnected = false;

    public static void onGameJoined(ClientPlayerEntity clientPlayer) {
        LiteBotClient.clientPlayer = clientPlayer;
        if (litebotServer) ClientNetworkHandler.respondHello();
    }

    public static ClientPlayerEntity getClientPlayer() {
        return clientPlayer;
    }

    public static boolean isLiteBotConnected() {
        return litebotConnected;
    }

    public static boolean isLitebotServer() {
        return litebotServer;
    }

    public static void disconnect() {
        if (litebotServer) {
            litebotServer = false;
            litebotConnected = false;
            clientPlayer = null;
        }
    }

    public static void setLitebotServer() {
        LiteBotClient.litebotServer = true;
    }

    public static void setLitebotConnected(boolean litebotConnected) {
        LiteBotClient.litebotConnected = litebotConnected;
    }
}
