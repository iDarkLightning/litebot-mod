package cf.litetech.litebotmod;

import cf.litetech.litebotmod.connection.Client;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import java.net.URI;
import java.net.URISyntaxException;

public class LitetechAdditions implements DedicatedServerModInitializer {
    private Client connection;

    @Override
    public void onInitializeServer() {
        this.connection = new Client(URI.create("ws://localhost:8000/server/recv_event"));
    }
}
