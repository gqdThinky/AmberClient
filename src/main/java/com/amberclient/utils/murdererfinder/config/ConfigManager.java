package com.amberclient.utils.murdererfinder.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.amberclient.utils.murdererfinder.MurdererFinder;
import com.amberclient.utils.murdererfinder.ModProperties;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConfigManager {
    private static boolean initialized = false;

    private static Config config = null;
    private static final Config defaults = new Config();

    private static File configFile;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    private static final Executor executor = Executors.newSingleThreadExecutor();

    public static void init() {
        if (initialized)
            return;

        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), ModProperties.MOD_ID + ".json");
        readConfig(false);

        // Ensure config is never null
        if (config == null) {
            MurdererFinder.logger.warn("Config is still null after initialization, using defaults");
            config = new Config();
            writeConfig(false);
        }

        initialized = true;
        MurdererFinder.logger.info("ConfigManager initialized successfully");
    }

    public static void readConfig(boolean async) {
        Runnable task = () -> {
            try {
                if (configFile.exists()) {
                    String fileContents = FileUtils.readFileToString(configFile, Charset.defaultCharset());

                    // Check if file content is empty or whitespace only
                    if (fileContents == null || fileContents.trim().isEmpty()) {
                        MurdererFinder.logger.warn("Config file is empty, creating new config");
                        writeNewConfig();
                        return;
                    }

                    Config loadedConfig = gson.fromJson(fileContents, Config.class);

                    // Check if gson returned null (invalid JSON)
                    if (loadedConfig == null) {
                        MurdererFinder.logger.warn("Config file contains invalid JSON, creating new config");
                        writeNewConfig();
                        return;
                    }

                    config = loadedConfig;

                    if (!config.validate()) {
                        MurdererFinder.logger.info("Config validation failed, fixing and saving");
                        writeConfig(true);
                    }
                } else {
                    MurdererFinder.logger.info("Config file doesn't exist, creating new one");
                    writeNewConfig();
                }
            } catch (Exception e) {
                MurdererFinder.logger.error("Error reading config file: " + e.getMessage());
                e.printStackTrace();
                writeNewConfig();
            }
        };

        if (async)
            executor.execute(task);
        else
            task.run();
    }

    public static void writeNewConfig() {
        try {
            config = new Config();
            writeConfig(false);
            MurdererFinder.logger.info("Created new config file");
        } catch (Exception e) {
            MurdererFinder.logger.error("Failed to create new config: " + e.getMessage());
            e.printStackTrace();
            // As a last resort, ensure config is not null
            if (config == null) {
                config = new Config();
            }
        }
    }

    public static void writeConfig() {
        writeConfig(true);
    }

    public static void writeConfig(boolean async) {
        Runnable task = () -> {
            try {
                if (config != null) {
                    String serialized = gson.toJson(config);
                    FileUtils.writeStringToFile(configFile, serialized, Charset.defaultCharset());
                } else {
                    MurdererFinder.logger.error("Cannot write config: config is null");
                }
            } catch (Exception e) {
                MurdererFinder.logger.error("Error writing config file: " + e.getMessage());
                e.printStackTrace();
            }
        };

        if (async)
            executor.execute(task);
        else
            task.run();
    }

    public static Config getConfig() {
        // Safety check - if config is somehow still null, return defaults
        if (config == null) {
            MurdererFinder.logger.warn("Config is null in getConfig(), returning defaults");
            return defaults;
        }
        return config;
    }

    public static Config getDefaults() {
        return defaults;
    }
}