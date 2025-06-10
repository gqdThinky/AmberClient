package com.amberclient.modules.movement

import com.amberclient.utils.module.Module
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class NoFall : Module("NoFall", "Prevents fall damage", "Movement") {
    companion object {
        const val MOD_ID = "amberclient-nofall"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    }

    override fun onTick() {
        val player: ClientPlayerEntity = client.player ?: return
        if (player.isSpectator || player.isCreative) return

        player.networkHandler.sendPacket(PlayerMoveC2SPacket.OnGroundOnly(true, false))
    }
}