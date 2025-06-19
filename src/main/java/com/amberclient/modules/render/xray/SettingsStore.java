package com.amberclient.modules.render.xray;

public class SettingsStore {
    private static SettingsStore instance;
    private final StateSettings settings;

    private SettingsStore() {
        settings = new StateSettings();
    }

    public static SettingsStore getInstance() {
        if (instance == null) {
            instance = new SettingsStore();
        }
        return instance;
    }

    public StateSettings get() {
        return settings;
    }

    public static class StateSettings {
        private boolean active = false;
        private boolean showLava = true;
        private boolean exposedOnly = false;
        private int halfRange = 4; // Chunks around the player

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isShowLava() {
            return showLava;
        }

        public boolean isExposedOnly() {
            return exposedOnly;
        }

        public void setExposedOnly(boolean exposedOnly) {
            this.exposedOnly = exposedOnly;
        }

        public int getHalfRange() {
            return halfRange;
        }
    }
}