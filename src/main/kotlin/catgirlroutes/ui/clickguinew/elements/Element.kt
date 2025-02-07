package catgirlroutes.ui.clickguinew.elements

import catgirlroutes.module.settings.Setting
import catgirlroutes.module.settings.impl.SelectorSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickguinew.ClickGUI
import net.minecraft.client.renderer.GlStateManager

abstract class Element<S: Setting<*>>(
    val parent: ModuleButton,
    val setting: S,
    val type: ElementType
) {
    val clickGui: ClickGUI = parent.window.clickGui

    var x = 7.0
    var y = 0.0

    val width = this.parent.width - 15.0
    var height: Double

    var displayName: String = this.setting.name
    var extended = false
    var listening = false

    val xAbsolute: Double
        get() = this.x + this.parent.x + this.parent.window.x

    val yAbsolute: Double
        get() = this.y + this.parent.y + this.parent.window.y


    init {
        this.height = when (this.type) {
            ElementType.TEXT_FIELD -> 25.0
            ElementType.SELECTOR -> 13.0
            ElementType.SLIDER -> 18.0
            else -> 11.0
        }
    }

    fun update() {
        this.displayName = this.setting.name
//        when (type) {
//            ElementType.SELECTOR -> {
//                height = if (extended)
//                    (((setting as? StringSelectorSetting)?.options?.size ?: (setting as SelectorSetting<*>).options.size) * (FontUtil.fontHeight + 2) + 13.0)
//                else
//                    13.0
//            }
//            else -> {}
//        }
    }

    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) : Double {
        GlStateManager.pushMatrix()
        GlStateManager.translate(this.x, this.y, 0.0)

        val elementLength = renderElement(mouseX, mouseY, partialTicks)

        GlStateManager.popMatrix()
        return elementLength + 5.0
    }

    protected open fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float) : Double { return this.height }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return this.isHovered(mouseX, mouseY)
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean { return false }

    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {  }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= this.xAbsolute && mouseX <= this.xAbsolute + this.width && mouseY >= this.yAbsolute && mouseY <= this.yAbsolute + this.height
    }

    companion object {
        const val DEFAULT_HEIGHT = 15
    }
}

