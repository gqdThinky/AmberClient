package com.amberclient.modules.combat;

import com.amberclient.events.EventManager;
import com.amberclient.events.PacketReceiveListener;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import com.amberclient.mixins.amberclient.EntityVelocityUpdateS2CPacketAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class Velocity extends Module implements PacketReceiveListener, ConfigurableModule {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    // Settings
    private final ModuleSetting explosions = new ModuleSetting("Explosions", "Should modifies blast knockback?", true);
    private final ModuleSetting fishing = new ModuleSetting("Fishing", "Should prevents being pulled by fishing rods?", false);

    private final List<ModuleSetting> settings = new ArrayList<>();

    public Velocity() {
        super("Velocity", "Prevents knockback by external forces", "Combat");

        settings.add(explosions);
        settings.add(fishing);
    }

    @Override
    public void onEnable() {
        EventManager.getInstance().add(PacketReceiveListener.class, this);
    }

    @Override
    public void onDisable() {
        EventManager.getInstance().remove(PacketReceiveListener.class, this);
    }

    @Override
    public void onPacketReceive(Packet<?> packet) {
        if (!isEnabled()) return;

        if (packet instanceof EntityVelocityUpdateS2CPacket velocityPacket) {
            assert mc.player != null;
            if (velocityPacket.getEntityId() == mc.player.getId()) {
                modifyVelocityPacket(velocityPacket);
            }
        }
    }

    private void modifyVelocityPacket(EntityVelocityUpdateS2CPacket packet) {
        if (mc.player == null) return;

        double currentVelX = mc.player.getVelocity().x;
        double currentVelY = mc.player.getVelocity().y;
        double currentVelZ = mc.player.getVelocity().z;

        double packetVelX = packet.getVelocityX() / 8000.0;
        double packetVelY = packet.getVelocityY() / 8000.0;
        double packetVelZ = packet.getVelocityZ() / 8000.0;

        double newVelX = currentVelX + (packetVelX - currentVelX);
        double newVelY = currentVelY + (packetVelY - currentVelY);
        double newVelZ = currentVelZ + (packetVelZ - currentVelZ);

        EntityVelocityUpdateS2CPacketAccessor accessor = (EntityVelocityUpdateS2CPacketAccessor) packet;
        accessor.setVelocityX((int) (newVelX * 8000));
        accessor.setVelocityY((int) (newVelY * 8000));
        accessor.setVelocityZ((int) (newVelZ * 8000));
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }
}