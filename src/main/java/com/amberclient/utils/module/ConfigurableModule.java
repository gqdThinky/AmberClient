package com.amberclient.utils.module;

import java.util.List;

public interface ConfigurableModule {
    /**
     * Returns the list of configurable parameters for this module
     * @return List of configurable parameters
     */
    List<ModuleSettings> getSettings();

    /**
     * Called when a parameter si modified
     * @param setting The changed parameter
     */
    default void onSettingChanged(ModuleSettings setting) {
        // null by default
    }
}
