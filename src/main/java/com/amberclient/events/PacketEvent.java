package com.amberclient.events;

import net.minecraft.network.packet.Packet;

public abstract class PacketEvent {
    public final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Send extends PacketEvent {
        public boolean cancelled = false;

        public Send(Packet<?> packet) {
            super(packet);
        }

        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}