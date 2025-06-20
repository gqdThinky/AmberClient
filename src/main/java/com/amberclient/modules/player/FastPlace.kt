package com.amberclient.modules.player

import com.amberclient.utils.module.Module
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.random.Random

class FastPlace : Module("FastPlace", "Blocks go brrrrrrr", "Player") {

    companion object {
        const val MOD_ID = "amberclient-fastplace"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)

        private var instance: FastPlace? = null

        @JvmField
        var isFastPlaceEnabled = false

        @JvmField
        var minDelay = 0

        @JvmField
        var maxDelay = 2

        @JvmField
        var stutterChance = 0.15f

        @JvmField
        var stutterMinDelay = 3

        @JvmField
        var stutterMaxDelay = 8

        @JvmStatic
        fun getRandomDelay(): Int {
            return if (Random.nextFloat() < stutterChance) {
                Random.nextInt(stutterMinDelay, stutterMaxDelay + 1)
            } else {
                Random.nextInt(minDelay, maxDelay + 1)
            }
        }

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