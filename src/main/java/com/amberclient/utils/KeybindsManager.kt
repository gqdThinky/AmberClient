package com.amberclient.utils

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object KeybindsManager {
    private val keyBindings = mutableMapOf<String, KeyBinding>()
    private var isInitialized = false

    val openClickGui: KeyBinding = registerKeyBinding(
        "key.amberclient.open_click_gui",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_SHIFT,
        "category.amberclient.hud",
        "Open Click GUI"
    )

    private fun registerKeyBinding(
        translationKey: String,
        type: InputUtil.Type,
        code: Int,
        category: String,
        displayName: String
    ): KeyBinding {
        if (keyBindings.containsKey(translationKey)) {
            throw IllegalArgumentException("Keybinding $translationKey is already registered")
        }
        val keyBinding = KeyBinding(translationKey, type, code, category)
        keyBindings[translationKey] = keyBinding
        return keyBinding
    }

    fun initialize() {
        if (isInitialized) return
        keyBindings.values.forEach { KeyBindingHelper.registerKeyBinding(it) }
        isInitialized = true
    }

    fun addKeyBinding(
        translationKey: String,
        type: InputUtil.Type = InputUtil.Type.KEYSYM,
        code: Int = GLFW.GLFW_KEY_UNKNOWN,
        category: String = "category.amberclient",
        displayName: String
    ): KeyBinding {
        return registerKeyBinding(translationKey, type, code, category, displayName)
    }
}