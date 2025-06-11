package com.amberclient.modules.player

import com.amberclient.utils.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class FastPlace : Module("FastPlace", "Blocks go brrrrrrr", "Player") {

    companion object {
        const val MOD_ID = "amberclient-fastplace"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)

        private var instance: FastPlace? = null

        @JvmField
        var isFastPlaceEnabled = false

        fun getInstance(): FastPlace? = instance
    }

    init {
        instance = this
    }

    override fun onEnable() {
        isFastPlaceEnabled = true
    }

    override fun onDisable() {
        isFastPlaceEnabled = false
    }
}