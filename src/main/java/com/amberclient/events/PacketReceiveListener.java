package com.amberclient.events;

import net.minecraft.network.packet.Packet;

public interface PacketReceiveListener {
    void onPacketReceive(Packet<?> packet);
}