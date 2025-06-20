package com.amberclient.modules.player

import com.amberclient.utils.module.ConfigurableModule
import com.amberclient.utils.module.Module
import com.amberclient.utils.module.ModuleSettings
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class FastBreak : Module("FastBreak", "Breaks blocks way faster", "Player"), ConfigurableModule {
    companion object {
        const val MOD_ID = "amberclient-fastbreak"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)

        @JvmStatic
        private var instance: FastBreak? = null

        @JvmStatic
        var isFastBreakEnabled: Boolean = false
            @JvmName("setFastBreakEnabled") set

        @JvmStatic
        fun getInstance(): FastBreak? {
            return instance
        }
    }

    private val activationChance: ModuleSettings
    private val legitMode: ModuleSettings
    private val settings: MutableList<ModuleSettings>

    init {
        instance = this
        isFastBreakEnabled = false

        activationChance = ModuleSettings(
            "Activation Chance",
            "Probability of FastBreak activating for a block. Lower values reduce anti-cheat detection.",
            1.0, 0.0, 1.0, 0.01
        )
        legitMode = ModuleSettings(
            "Legit Mode",
            "Only removes block-breaking delay without speeding up the process. Safer for anti-cheat.",
            false
        )

        settings = mutableListOf(activationChance, legitMode)
    }

    override fun onEnable() {
        isFastBreakEnabled = true
    }

    override fun onDisable() {
        isFastBreakEnabled = false
    }

    override fun getSettings(): List<ModuleSettings> {
        return settings
    }

    fun getActivationChance(): Double {
        return activationChance.getDoubleValue()
    }

    fun isLegitMode(): Boolean {
        return legitMode.getBooleanValue()
    }
}

