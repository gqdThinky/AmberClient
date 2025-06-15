package com.amberclient.utils.keybinds

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

    val autoClutchKey: KeyBinding = registerKeyBinding(
        "key.amberclient.auto_clutch_key",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "category.amberclient.modules",
        "Toggle Auto Clutch Key"
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
        val keyBinding = registerKeyBinding(translationKey, type, code, category, displayName)
        KeyBindingHelper.registerKeyBinding(keyBinding)
        return keyBinding
    }

    fun getKeyCodeFromName(keyName: String): Int {
        return try {
            val glfwKeyName = "GLFW_KEY_" + keyName.uppercase()
            val field = GLFW::class.java.getField(glfwKeyName)
            field.getInt(null)
        } catch (e: Exception) {
            GLFW.GLFW_KEY_UNKNOWN
        }
    }

    fun findKeyBindingByKeyCode(keyCode: Int): KeyBinding? {
        return keyBindings.values.find { keyBinding ->
            try {
                val boundKeyField = KeyBinding::class.java.getDeclaredField("boundKey")
                boundKeyField.isAccessible = true
                val boundKey = boundKeyField.get(keyBinding) as InputUtil.Key
                boundKey.code == keyCode
            } catch (e: Exception) {
                false
            }
        }
    }

    // Add this method that was missing
    fun getKeyBindings(): Map<String, KeyBinding> {
        return keyBindings.toMap()
    }
}