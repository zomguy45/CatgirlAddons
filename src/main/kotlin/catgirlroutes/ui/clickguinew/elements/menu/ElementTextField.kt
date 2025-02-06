package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.ui.misc.elements.impl.MiscElementText
import org.lwjgl.input.Keyboard
import java.awt.Color

class ElementTextField(parent: ModuleButton, setting: StringSetting) :
    Element<StringSetting>(parent, setting, ElementType.TEXT_FIELD) {

    private val textField = MiscElementText(
        0.0,
        fontHeight + 3.0,
        width,
        12.0,
        this.setting.text,
        thickness = 1.0,
        bgColour = Color(ColorUtil.bgColor)
    )

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        FontUtil.drawString(displayName, 0.0, 0.0)
        this.textField.render(mouseX, mouseY)
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return this.textField.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        this.textField.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (this.textField.keyTyped(typedChar, keyCode)) {
            this.setting.text = this.textField.text
        }
        return super.keyTyped(typedChar, keyCode)
    }

    companion object {
        val keyBlackList = intArrayOf(
            Keyboard.KEY_LSHIFT,
            Keyboard.KEY_RSHIFT,
            Keyboard.KEY_UP,
            Keyboard.KEY_RIGHT,
            Keyboard.KEY_LEFT,
            Keyboard.KEY_DOWN,
            Keyboard.KEY_END,
            Keyboard.KEY_NUMLOCK,
            Keyboard.KEY_DELETE,
            Keyboard.KEY_LCONTROL,
            Keyboard.KEY_RCONTROL,
            Keyboard.KEY_CAPITAL,
            Keyboard.KEY_LMENU,
            Keyboard.KEY_F1,
            Keyboard.KEY_F2,
            Keyboard.KEY_F3,
            Keyboard.KEY_F4,
            Keyboard.KEY_F5,
            Keyboard.KEY_F6,
            Keyboard.KEY_F7,
            Keyboard.KEY_F8,
            Keyboard.KEY_F9,
            Keyboard.KEY_F10,
            Keyboard.KEY_F11,
            Keyboard.KEY_F12,
            Keyboard.KEY_F13,
            Keyboard.KEY_F14,
            Keyboard.KEY_F15,
            Keyboard.KEY_F16,
            Keyboard.KEY_F17,
            Keyboard.KEY_F18,
            Keyboard.KEY_F19,
            Keyboard.KEY_SCROLL,
            Keyboard.KEY_RMENU,
            Keyboard.KEY_LMETA,
            Keyboard.KEY_RMETA,
            Keyboard.KEY_FUNCTION,
            Keyboard.KEY_PRIOR,
            Keyboard.KEY_NEXT,
            Keyboard.KEY_INSERT,
            Keyboard.KEY_HOME,
            Keyboard.KEY_PAUSE,
            Keyboard.KEY_APPS,
            Keyboard.KEY_POWER,
            Keyboard.KEY_SLEEP,
            Keyboard.KEY_SYSRQ,
            Keyboard.KEY_CLEAR,
            Keyboard.KEY_SECTION,
            Keyboard.KEY_UNLABELED,
            Keyboard.KEY_KANA,
            Keyboard.KEY_CONVERT,
            Keyboard.KEY_NOCONVERT,
            Keyboard.KEY_YEN,
            Keyboard.KEY_CIRCUMFLEX,
            Keyboard.KEY_AT,
            Keyboard.KEY_UNDERLINE,
            Keyboard.KEY_KANJI,
            Keyboard.KEY_STOP,
            Keyboard.KEY_AX,
            Keyboard.KEY_TAB,
        )
    }
}