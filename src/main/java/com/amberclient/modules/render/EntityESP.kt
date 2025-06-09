package com.amberclient.modules.render

import com.amberclient.utils.module.ConfigurableModule
import com.amberclient.utils.module.Module
import com.amberclient.utils.module.ModuleSetting
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

class EntityESP : Module("Entity ESP", "Display outlines around entity models (players & mobs)", "Render"), ConfigurableModule {
    companion object {
        const val MOD_ID = "amberclient-entityesp"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)

        @JvmStatic
        val stateToEntity: MutableMap<LivingEntityRenderState, LivingEntity> = WeakHashMap()

        private var INSTANCE: EntityESP? = null

        @JvmStatic
        fun getInstance(): EntityESP? = INSTANCE
    }

    private val renderPlayersSetting: ModuleSetting
    private val renderMobsSetting: ModuleSetting
    private val rangeSetting: ModuleSetting
    private val entityInfosSetting: ModuleSetting
    private val settings: MutableList<ModuleSetting>

    init {
        INSTANCE = this
        renderPlayersSetting = ModuleSetting("Render Players", "Displays outlines for players", true)
        renderMobsSetting = ModuleSetting("Render Mobs", "Displays outlines for mobs", false)
        rangeSetting = ModuleSetting("Render Range", "X (in chunks)", 4.0, 1.0, 8.0, 1.0)
        entityInfosSetting = ModuleSetting("Entity Infos", "Shows nametags and health above entities", true)

        settings = mutableListOf<ModuleSetting>().apply {
            add(renderPlayersSetting)
            add(renderMobsSetting)
            add(rangeSetting)
            add(entityInfosSetting)
        }
    }

    override fun onEnable() {
        val client = MinecraftClient.getInstance()
        LOGGER.info("$name module enabled")
    }

    override fun onDisable() {
        val client = MinecraftClient.getInstance()
        client.player?.sendMessage(
            Text.literal("§4[§cAmberClient§4] §c§l${name} §r§cdeactivated"),
            true
        )
        LOGGER.info("$name module disabled")
    }

    override fun getSettings(): List<ModuleSetting> = settings

    override fun onSettingChanged(setting: ModuleSetting) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return

        when (setting) {
            renderPlayersSetting -> {
                player.sendMessage(
                    Text.literal("§6EntityESP settings updated: renderPlayers=${renderPlayersSetting.booleanValue}"),
                    true
                )
                LOGGER.info("EntityESP settings updated: renderPlayers={}", renderPlayersSetting.booleanValue)
            }
            renderMobsSetting -> {
                player.sendMessage(
                    Text.literal("§6EntityESP settings updated: renderMobs=${renderMobsSetting.booleanValue}"),
                    true
                )
                LOGGER.info("EntityESP settings updated: renderMobs={}", renderMobsSetting.booleanValue)
            }
            rangeSetting -> {
                player.sendMessage(
                    Text.literal("§6EntityESP settings updated: range=${rangeSetting.doubleValue}"),
                    true
                )
                LOGGER.info("EntityESP settings updated: range={}", rangeSetting.doubleValue)
            }
            entityInfosSetting -> {
                player.sendMessage(
                    Text.literal("§6EntityESP settings updated: entityInfos=${entityInfosSetting.booleanValue}"),
                    true
                )
                LOGGER.info("EntityESP settings updated: entityInfos={}", entityInfosSetting.booleanValue)
            }
        }
    }

    fun getRenderPlayersSetting(): ModuleSetting = renderPlayersSetting
    fun getRenderMobsSetting(): ModuleSetting = renderMobsSetting
    fun getEntityInfosSetting(): ModuleSetting = entityInfosSetting
    fun getRangeSetting(): ModuleSetting = rangeSetting
}