package com.amberclient.modules.combat;

import com.amberclient.events.EventManager;
import com.amberclient.events.PacketReceiveListener;
import com.amberclient.mixins.amberclient.EntityVelocityUpdateS2CPacketAccessor;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class Velocity extends Module implements PacketReceiveListener, ConfigurableModule {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final ModuleSettings horizontalMultiplier = new ModuleSettings("Horizontal Multiplier", "X/Z knockback multiplier", 0.0, 0.0, 1.0, 0.05);
    private final ModuleSettings verticalMultiplier = new ModuleSettings("Vertical Multiplier", "Y knockback multiplier", 0.0, 0.0, 1.0, 0.05);

    private final List<ModuleSettings> settings = new ArrayList<>();

    public Velocity() {
        super("Velocity", "Reduces or removes knockback", "Combat");
        settings.add(horizontalMultiplier);
        settings.add(verticalMultiplier);
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
        if (!isEnabled() || !(packet instanceof EntityVelocityUpdateS2CPacket)) return;

        EntityVelocityUpdateS2CPacket velocityPacket = (EntityVelocityUpdateS2CPacket) packet;

        if (mc.player != null && velocityPacket.getEntityId() == mc.player.getId()) {
            double multiplierXZ = horizontalMultiplier.getDoubleValue();
            double multiplierY = verticalMultiplier.getDoubleValue();

            EntityVelocityUpdateS2CPacketAccessor accessor = (EntityVelocityUpdateS2CPacketAccessor) velocityPacket;

            int originalX = accessor.getVelocityX();
            int originalY = accessor.getVelocityY();
            int originalZ = accessor.getVelocityZ();

            accessor.setVelocityX((int) (originalX * multiplierXZ));
            accessor.setVelocityY((int) (originalY * multiplierY));
            accessor.setVelocityZ((int) (originalZ * multiplierXZ));
        }
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }
}
