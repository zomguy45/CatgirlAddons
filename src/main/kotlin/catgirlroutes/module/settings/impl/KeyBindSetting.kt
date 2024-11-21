package catgirlroutes.module.settings.impl

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.Visibility
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

// odon clint inspiration
class KeyBindSetting(
    name: String,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
    override val default: Keybinding
) : Setting<Keybinding>(name, visibility, description) {

    override var value: Keybinding = default;

    /**
     * Action to do, when keybinding is pressed
     *
     * Note: Action is always invoked, even if module isn't enabled.
     */
    fun onPress(block: () -> Unit): KeyBindSetting {
        value.onPress = block
        return this
    }

    override fun reset() {
        value.key = default.key
    }
}

class Keybinding(var key: Int) {

    /**
     * Intended to active when keybind is pressed.
     */
    var onPress: (() -> Unit)? = null

    /**
     * @return `true` if [key] is held down.
     */
    fun isDown(): Boolean {
        return if (key == 0) false else (if (key < 0) Mouse.isButtonDown(key + 100) else Keyboard.isKeyDown(key))
    }
}