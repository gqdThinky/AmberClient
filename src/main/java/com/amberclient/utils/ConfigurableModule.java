package com.amberclient.utils;

import java.util.List;

public interface ConfigurableModule {
    /**
     * Returns the list of configurable parameters for this module
     * @return List of configurable parameters
     */
    List<ModuleSetting> getSettings();

    /**
     * Called when a parameter si modified
     * @param setting The changed parameter
     */
    default void onSettingChanged(ModuleSetting setting) {
        // null by default
    }
}
