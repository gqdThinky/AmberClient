package com.amberclient.utils.keybinds

import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW
import java.util.concurrent.ConcurrentHashMap

object CustomKeybindManager {
    private val keyBindings = ConcurrentHashMap<Int, MutableList<KeybindAction>>()
    private val keyStates = ConcurrentHashMap<Int, Boolean>()
    private var isInitialized = false

    data class KeybindAction(
        val id: String,
        val description: String,
        val callback: Runnable,
        val requiresPlayer: Boolean = true
    )

    fun initialize() {
        if (isInitialized) return
        isInitialized = true
        println("[CustomKeybindManager] Initialized custom keybind system")
    }

    fun bindKey(
        keyCode: Int,
        actionId: String,
        description: String,
        requiresPlayer: Boolean = true,
        callback: Runnable
    ) {
        val action = KeybindAction(actionId, description, callback, requiresPlayer)
        keyBindings.computeIfAbsent(keyCode) { mutableListOf() }.add(action)
        keyStates[keyCode] = false
        println("[CustomKeybindManager] Bound key ${getKeyName(keyCode)} to action: $description")
    }

    fun unbindAction(keyCode: Int, actionId: String) {
        keyBindings[keyCode]?.removeIf { it.id == actionId }
        if (keyBindings[keyCode]?.isEmpty() == true) {
            keyBindings.remove(keyCode)
            keyStates.remove(keyCode)
        }
    }

    fun unbindKey(keyCode: Int) {
        keyBindings.remove(keyCode)
        keyStates.remove(keyCode)
    }

    fun isKeyBound(keyCode: Int): Boolean {
        return keyBindings.containsKey(keyCode) && keyBindings[keyCode]?.isNotEmpty() == true
    }

    fun getKeyActions(keyCode: Int): List<KeybindAction> {
        return keyBindings[keyCode]?.toList() ?: emptyList()
    }

    fun tick() {
        if (!isInitialized) return

        val client = MinecraftClient.getInstance()
        val window = client.window?.handle ?: return

        keyBindings.keys.forEach { keyCode ->
            val isPressed = GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS
            val wasPressed = keyStates[keyCode] ?: false

            if (isPressed && !wasPressed) {
                executeKeyActions(keyCode)
            }

            keyStates[keyCode] = isPressed
        }
    }

    private fun executeKeyActions(keyCode: Int) {
        val actions = keyBindings[keyCode] ?: return
        val client = MinecraftClient.getInstance()

        actions.forEach { action ->
            try {
                if (action.requiresPlayer && client.player == null) {
                    return@forEach
                }

                action.callback.run()

            } catch (e: Exception) {
                println("[CustomKeybindManager] Error executing action ${action.id}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun getKeyName(keyCode: Int): String {
        return when (keyCode) {
            GLFW.GLFW_KEY_UNKNOWN -> "Unknown"
            GLFW.GLFW_KEY_SPACE -> "Space"
            GLFW.GLFW_KEY_APOSTROPHE -> "'"
            GLFW.GLFW_KEY_COMMA -> ","
            GLFW.GLFW_KEY_MINUS -> "-"
            GLFW.GLFW_KEY_PERIOD -> "."
            GLFW.GLFW_KEY_SLASH -> "/"
            GLFW.GLFW_KEY_SEMICOLON -> ";"
            GLFW.GLFW_KEY_EQUAL -> "="
            GLFW.GLFW_KEY_LEFT_BRACKET -> "["
            GLFW.GLFW_KEY_BACKSLASH -> "\\"
            GLFW.GLFW_KEY_RIGHT_BRACKET -> "]"
            GLFW.GLFW_KEY_GRAVE_ACCENT -> "`"
            GLFW.GLFW_KEY_ESCAPE -> "Escape"
            GLFW.GLFW_KEY_ENTER -> "Enter"
            GLFW.GLFW_KEY_TAB -> "Tab"
            GLFW.GLFW_KEY_BACKSPACE -> "Backspace"
            GLFW.GLFW_KEY_INSERT -> "Insert"
            GLFW.GLFW_KEY_DELETE -> "Delete"
            GLFW.GLFW_KEY_RIGHT -> "Right Arrow"
            GLFW.GLFW_KEY_LEFT -> "Left Arrow"
            GLFW.GLFW_KEY_DOWN -> "Down Arrow"
            GLFW.GLFW_KEY_UP -> "Up Arrow"
            GLFW.GLFW_KEY_PAGE_UP -> "Page Up"
            GLFW.GLFW_KEY_PAGE_DOWN -> "Page Down"
            GLFW.GLFW_KEY_HOME -> "Home"
            GLFW.GLFW_KEY_END -> "End"
            GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock"
            GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll Lock"
            GLFW.GLFW_KEY_NUM_LOCK -> "Num Lock"
            GLFW.GLFW_KEY_PRINT_SCREEN -> "Print Screen"
            GLFW.GLFW_KEY_PAUSE -> "Pause"
            GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift"
            GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Ctrl"
            GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt"
            GLFW.GLFW_KEY_LEFT_SUPER -> "Left Super"
            GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift"
            GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Ctrl"
            GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt"
            GLFW.GLFW_KEY_RIGHT_SUPER -> "Right Super"
            in GLFW.GLFW_KEY_0..GLFW.GLFW_KEY_9 -> (keyCode - GLFW.GLFW_KEY_0 + '0'.code).toChar().toString()
            in GLFW.GLFW_KEY_A..GLFW.GLFW_KEY_Z -> (keyCode - GLFW.GLFW_KEY_A + 'A'.code).toChar().toString()
            in GLFW.GLFW_KEY_F1..GLFW.GLFW_KEY_F25 -> "F${keyCode - GLFW.GLFW_KEY_F1 + 1}"
            in GLFW.GLFW_KEY_KP_0..GLFW.GLFW_KEY_KP_9 -> "Numpad ${keyCode - GLFW.GLFW_KEY_KP_0}"
            GLFW.GLFW_KEY_KP_DECIMAL -> "Numpad ."
            GLFW.GLFW_KEY_KP_DIVIDE -> "Numpad /"
            GLFW.GLFW_KEY_KP_MULTIPLY -> "Numpad *"
            GLFW.GLFW_KEY_KP_SUBTRACT -> "Numpad -"
            GLFW.GLFW_KEY_KP_ADD -> "Numpad +"
            GLFW.GLFW_KEY_KP_ENTER -> "Numpad Enter"
            GLFW.GLFW_KEY_KP_EQUAL -> "Numpad ="
            else -> "Key $keyCode"
        }
    }

    fun getKeyCodeFromName(keyName: String): Int {
        val upperName = keyName.uppercase()

        return when (upperName) {
            "SPACE" -> GLFW.GLFW_KEY_SPACE
            "ENTER" -> GLFW.GLFW_KEY_ENTER
            "TAB" -> GLFW.GLFW_KEY_TAB
            "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE
            "DELETE" -> GLFW.GLFW_KEY_DELETE
            "ESCAPE", "ESC" -> GLFW.GLFW_KEY_ESCAPE
            "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT
            "CTRL", "CONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL
            "ALT" -> GLFW.GLFW_KEY_LEFT_ALT
            "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT
            "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT
            "LCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL
            "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL
            "LALT" -> GLFW.GLFW_KEY_LEFT_ALT
            "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT
            "UP" -> GLFW.GLFW_KEY_UP
            "DOWN" -> GLFW.GLFW_KEY_DOWN
            "LEFT" -> GLFW.GLFW_KEY_LEFT
            "RIGHT" -> GLFW.GLFW_KEY_RIGHT
            else -> {
                // Try single character
                if (upperName.length == 1) {
                    val char = upperName[0]
                    when (char) {
                        in '0'..'9' -> GLFW.GLFW_KEY_0 + (char - '0')
                        in 'A'..'Z' -> GLFW.GLFW_KEY_A + (char - 'A')
                        else -> GLFW.GLFW_KEY_UNKNOWN
                    }
                }
                // Try function keys
                else if (upperName.startsWith("F") && upperName.length <= 3) {
                    try {
                        val num = upperName.substring(1).toInt()
                        if (num in 1..25) GLFW.GLFW_KEY_F1 + (num - 1)
                        else GLFW.GLFW_KEY_UNKNOWN
                    } catch (e: NumberFormatException) {
                        GLFW.GLFW_KEY_UNKNOWN
                    }
                } else {
                    GLFW.GLFW_KEY_UNKNOWN
                }
            }
        }
    }

    fun getAllBindings(): Map<Int, List<KeybindAction>> {
        return keyBindings.toMap()
    }

    fun clearAllBindings() {
        keyBindings.clear()
        keyStates.clear()
    }
}