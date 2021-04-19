package cf.litetech.litebotmod;

import cf.litetech.litebotmod.connection.Client;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;

import java.net.URI;

public class LiteBotMod implements DedicatedServerModInitializer {
    private final static Client connection = new Client(URI.create("ws://localhost:8000/server/recv_event"));
    private static MinecraftServer server;
    private final static Bridge bridge = new Bridge();

    @Override
    public void onInitializeServer() {
        connection.connect();
    }

    public static void setServer(MinecraftServer server) {
        LiteBotMod.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static Client getConnection() {
        return connection;
    }

    public static Bridge getBridge() {
        return bridge;
    }

}
