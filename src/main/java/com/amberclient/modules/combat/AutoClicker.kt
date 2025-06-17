package com.amberclient.modules.combat

import com.amberclient.utils.module.ConfigurableModule
import com.amberclient.utils.module.Module
import com.amberclient.utils.module.ModuleSettings
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class AutoClicker : Module("AutoClicker", "Auto clicks when aiming at an entity", "Combat"), ConfigurableModule {

    companion object {
        const val MOD_ID = "amberclient-autoclicker"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    }

    private val useWeaponSpeed: ModuleSettings
    private val clickDelay: ModuleSettings
    private var lastClickTime: Long = 0
    private val settings: MutableList<ModuleSettings>

    init {
        useWeaponSpeed = ModuleSettings("Use Weapon Speed", "Use the attack delay of the held item", true)
        clickDelay = ModuleSettings("Default Click Delay", "Delay between clicks in ms (when weapon speed is disabled)", 100.0, 50.0, 500.0, 10.0)

        settings = mutableListOf<ModuleSettings>().apply {
            add(useWeaponSpeed)
            add(clickDelay)
        }
    }

    private fun getWeaponAttackDelay(): Double {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return 100.0

        val heldItem: ItemStack = player.mainHandStack
        if (heldItem.isEmpty) {
            val baseAttackSpeed = player.getAttributeValue(EntityAttributes.ATTACK_SPEED)
            return 1000.0 / baseAttackSpeed
        }

        val attackSpeed = player.getAttributeValue(EntityAttributes.ATTACK_SPEED)
        return 1000.0 / attackSpeed
    }

    override fun onTick() {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return
        val world = client.world ?: return

        val hitResult = client.crosshairTarget
        if (hitResult != null && hitResult.type == HitResult.Type.ENTITY) {
            val entityHitResult = hitResult as EntityHitResult
            val target: Entity = entityHitResult.entity
            val currentTime = System.currentTimeMillis()

            val delayToUse = if (useWeaponSpeed.booleanValue) {
                getWeaponAttackDelay()
            } else {
                clickDelay.doubleValue
            }

            if (currentTime - lastClickTime >= delayToUse) {
                client.interactionManager?.let { interactionManager ->
                    interactionManager.attackEntity(player, target)
                    player.swingHand(Hand.MAIN_HAND)
                    lastClickTime = currentTime
                }
            }
        }
    }

    override fun getSettings(): List<ModuleSettings> = settings

    override fun onSettingChanged(setting: ModuleSettings) {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return

        when (setting) {
            clickDelay -> {
                player.sendMessage(
                    Text.literal("§6AutoClicker settings updated: Click Delay=${clickDelay.doubleValue} ms"),
                    true
                )
                LOGGER.info("AutoClicker settings updated: Click Delay={} ms", clickDelay.doubleValue)
            }
            useWeaponSpeed -> {
                val status = if (useWeaponSpeed.booleanValue) "enabled" else "disabled"
                player.sendMessage(
                    Text.literal("§6AutoClicker settings updated: Use Weapon Speed=$status"),
                    true
                )
                LOGGER.info("AutoClicker settings updated: Use Weapon Speed={}", status)
            }
        }
    }
}