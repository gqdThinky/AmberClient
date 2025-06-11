package com.amberclient.modules.hud

import com.amberclient.utils.module.Module
import com.amberclient.utils.module.ConfigurableModule
import com.amberclient.utils.module.ModuleSetting
import net.minecraft.text.Text

class ActiveMods : Module("Active mods", "Toggles display of active modules", "HUD"), ConfigurableModule {

    private val enableRGB: ModuleSetting
    private val settings: MutableList<ModuleSetting>

    init {
        enabled = true

        enableRGB = ModuleSetting("Enable RGB", "Use animated RGB text color", true)

        settings = mutableListOf<ModuleSetting>().apply {
            add(enableRGB)
        }
    }

    fun isRGBEnabled(): Boolean = enableRGB.getBooleanValue()

    private fun updateRGBSetting() {
        client.player?.sendMessage(
            Text.literal("§6RGB enabled: §l${isRGBEnabled()}"),
            true
        )
    }

    override fun getSettings(): List<ModuleSetting> = settings

    override fun onSettingChanged(setting: ModuleSetting) {
        if (setting == enableRGB) {
            updateRGBSetting()
        }
    }
}