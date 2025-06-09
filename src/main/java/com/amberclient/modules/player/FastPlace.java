package com.amberclient.modules.player;

import com.amberclient.utils.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FastPlace extends Module {
    public static final String MOD_ID = "amberclient-fastplace";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static FastPlace instance;
    public static boolean isFastPlaceEnabled;

    public FastPlace() {
        super("FastPlace", "Blocks go brrrrrrr", "Player");
        instance = this;
    }

    public static FastPlace getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();

        isFastPlaceEnabled = true;
        LOGGER.info(getName() + " module enabled");
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();

        isFastPlaceEnabled = false;
        LOGGER.info(getName() + " module disabled");
    }
}