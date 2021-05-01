package cf.litetech.litebotmod;

import cf.litetech.litebotmod.config.Config;
import cf.litetech.litebotmod.config.ConfigFile;
import cf.litetech.litebotmod.connection.Client;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LiteBotMod implements DedicatedServerModInitializer {
    private static Client connection;
    private static MinecraftServer server;
    private static Bridge bridge;
    private static final ConfigFile configFile = new ConfigFile("litebot.json");
    private static final List<LiteBotExtension> EXTENSIONS = new ArrayList<>();
    public static Config config = ConfigFile.DEFAULT_CONFIG;
    public static Logger LOGGER = LogManager.getLogger("LiteBot-Mod");


    @Override
    public void onInitializeServer() {
        if (!readConfig()) {
            LOGGER.warn("Cannot connect to LiteBot since the config was just created! Please fill it out and restart!");
            return;
        }

        connection = new Client(URI.create("ws://" + config.litebotAddress + "/server/"));
        bridge = new Bridge();
        connection.connect();
    }

    public static void addExtension(LiteBotExtension extension) {
        EXTENSIONS.add(extension);
        extension.registerHooks();
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

    public static List<LiteBotExtension> getExtensions() {
        return EXTENSIONS;
    }

    public static boolean readConfig() {
        boolean result = true;
        if (!configFile.exists()) {
            LOGGER.warn("Config file " + configFile + " does not exist, creating...");
            configFile.writeDefaultConfig();
            result = false;
        }

        Optional<Config> readResult = configFile.readConfig();
        if (!readResult.isPresent()) {
            LOGGER.error("Failed to read config file " + configFile);
        } else {
            LOGGER.info("Read config " + configFile);
            config = readResult.get();
        }
        return result;
    }
}
