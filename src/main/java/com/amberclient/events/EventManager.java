package com.amberclient.events;

import net.minecraft.network.packet.Packet;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static final EventManager INSTANCE = new EventManager();
    private final List<PreMotionListener> preMotionListeners = new ArrayList<>();
    private final List<PostMotionListener> postMotionListeners = new ArrayList<>();
    private final List<PacketReceiveListener> packetReceiveListeners = new ArrayList<>();
    private final List<PreVelocityListener> preVelocityListeners = new ArrayList<>(); // Add this
    private final List<PostVelocityListener> postVelocityListeners = new ArrayList<>();

    public EventManager() {
    }

    public static EventManager getInstance() {
        return INSTANCE;
    }

    public void add(Class<?> type, Object listener) {
        if (type == PreMotionListener.class && listener instanceof PreMotionListener) {
            preMotionListeners.add((PreMotionListener) listener);
        } else if (type == PostMotionListener.class && listener instanceof PostMotionListener) {
            postMotionListeners.add((PostMotionListener) listener);
        } else if (type == PacketReceiveListener.class && listener instanceof PacketReceiveListener) {
            packetReceiveListeners.add((PacketReceiveListener) listener);
        } else if (type == PreVelocityListener.class && listener instanceof PreVelocityListener) { // Add this
            preVelocityListeners.add((PreVelocityListener) listener);
        } else if (type == PostVelocityListener.class && listener instanceof PostVelocityListener) {
            postVelocityListeners.add((PostVelocityListener) listener);
        }
    }

    public void remove(Class<?> type, Object listener) {
        if (type == PreMotionListener.class) {
            preMotionListeners.remove(listener);
        } else if (type == PostMotionListener.class) {
            postMotionListeners.remove(listener);
        } else if (type == PacketReceiveListener.class) {
            packetReceiveListeners.remove(listener);
        } else if (type == PreVelocityListener.class) { // Add this
            preVelocityListeners.remove(listener);
        } else if (type == PostVelocityListener.class) {
            postVelocityListeners.remove(listener);
        }
    }

    public void firePreMotion() {
        for (PreMotionListener listener : new ArrayList<>(preMotionListeners)) {
            listener.onPreMotion();
        }
    }

    public void firePostMotion() {
        for (PostMotionListener listener : new ArrayList<>(postMotionListeners)) {
            listener.onPostMotion();
        }
    }

    public void firePacketReceive(Packet<?> packet) {
        for (PacketReceiveListener listener : new ArrayList<>(packetReceiveListeners)) {
            listener.onPacketReceive(packet);
        }
    }

    public void firePreVelocity(PreVelocityEvent event) { // Add this
        for (PreVelocityListener listener : new ArrayList<>(preVelocityListeners)) {
            listener.onPreVelocity(event);
        }
    }

    public void firePostVelocity(PostVelocityEvent event) {
        for (PostVelocityListener listener : new ArrayList<>(postVelocityListeners)) {
            listener.onPostVelocity(event);
        }
    }
}