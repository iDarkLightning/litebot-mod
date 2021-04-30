package cf.litetech.litebotmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class ConfigFile {

    public static final Config DEFAULT_CONFIG = new Config();

    private static final File FABRIC_CONFIG_DIR = FabricLoader.getInstance().getConfigDir().toFile();
    private static final Gson CONFIG_SERIALIZER = new GsonBuilder().setPrettyPrinting().create();

    private final File file;

    public ConfigFile(String fileName) {
        file = new File(FABRIC_CONFIG_DIR, fileName);
    }

    @Override
    public String toString() {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    public boolean exists() {
        return file.exists();
    }

    public Optional<Config> readConfig() {
        try (FileReader reader = new FileReader(file)) {
            return Optional.of(CONFIG_SERIALIZER.fromJson(reader, Config.class));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void writeConfig(Config config) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(CONFIG_SERIALIZER.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeDefaultConfig() {
        writeConfig(DEFAULT_CONFIG);
    }
}

