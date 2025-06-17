package com.amberclient.modules.combat

import com.amberclient.events.EventManager
import com.amberclient.events.PreVelocityEvent
import com.amberclient.events.PreVelocityListener
import com.amberclient.utils.module.ConfigurableModule
import com.amberclient.utils.module.Module
import com.amberclient.utils.module.ModuleSettings
import net.minecraft.client.MinecraftClient

class Velocity : Module("Velocity", "Reduces knockback with anti-rollback", "Combat"), ConfigurableModule, PreVelocityListener {

    companion object {
        private var instance: Velocity? = null

        fun getInstance(): Velocity? = instance
    }

    private val horizontalScale = ModuleSettings("Horizontal Scale", "X/Z velocity scale", 0.6, 0.0, 5.0, 0.05)
    private val verticalScale = ModuleSettings("Vertical Scale", "Y velocity scale", 0.4, 0.0, 5.0, 0.05)
    private val chance = ModuleSettings("Chance", "Chance to apply velocity reduction", 100.0, 0.0, 100.0, 5.0)
    private val cancelAir = ModuleSettings("Cancel Air", "Cancel velocity when in air", false)

    private val settings: MutableList<ModuleSettings>
    private val client = MinecraftClient.getInstance()

    init {
        settings = mutableListOf<ModuleSettings>().apply {
            add(horizontalScale)
            add(verticalScale)
            add(chance)
            add(cancelAir)
        }
        instance = this
    }

    override fun onEnable() {
        super.onEnable()
        EventManager.getInstance().add(PreVelocityListener::class.java, this)
    }

    override fun onDisable() {
        super.onDisable()
        EventManager.getInstance().remove(PreVelocityListener::class.java, this)
    }

    override fun onPreVelocity(event: PreVelocityEvent) {
        val player = client.player ?: return

        if (cancelAir.booleanValue && !player.isOnGround) {
            event.isCanceled = true
            return
        }

        // Get packet velocity (already scaled to blocks per tick in VelocityMixin)
        var motionX = event.motionX
        var motionY = event.motionY
        var motionZ = event.motionZ

        // Apply velocity reduction based on chance
        if (chance.doubleValue == 100.0 || Math.random() * 100 <= chance.doubleValue) {
            motionX *= horizontalScale.doubleValue
            motionY *= verticalScale.doubleValue
            motionZ *= horizontalScale.doubleValue
        }

        // Update the event with modified velocity
        event.motionX = motionX
        event.motionY = motionY
        event.motionZ = motionZ
    }

    override fun getSettings(): List<ModuleSettings> = settings
}