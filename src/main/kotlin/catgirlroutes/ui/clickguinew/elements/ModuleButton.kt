package catgirlroutes.ui.clickguinew.elements

import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.*
import catgirlroutes.ui.animations.impl.ColorAnimation
import catgirlroutes.ui.animations.impl.EaseInOutAnimation
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickguinew.Window
import catgirlroutes.ui.clickguinew.elements.menu.*
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.render.HUDRenderUtils.drawOutlinedRectBorder
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import catgirlroutes.utils.wrapText
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color


/*
    TODO: height shit
        account for description in expand animation
        animate description text
        animate elements
        animate
        animate
        animate
        finish elements
        change selector element
 */
class ModuleButton(val module: Module, val window: Window) {
    val menuElements: ArrayList<Element<*>> = ArrayList()

    var x = 0.0
    var y = 0.0

    val width = window.width / 2
    val height: Double
        get() = 25.0 + if (extended) 0.0 else (fontHeight * description.size).toDouble()

    val actualHeight: Double
        get() = this.height + if (extended) this.length - 25.0 else 0.0

    var length = 0.0

    var extended = false

    val xAbsolute: Double
        get() = x + window.x
    val yAbsolute: Double
        get() = y + window.y

    private val description: List<String> = wrapText(this.module.description, this.width - 50.0)
    private val extendAnimation = EaseInOutAnimation(500)
    private val lineAnimation = EaseInOutAnimation(1000)
    private val colourAnimation = ColorAnimation(100)

    val animatedHeight: Double
        get() = this.height + extendAnimation.get(0.0, this.height + getSettingHeight(), !extended)

    init {
        updateElements()
    }

    fun updateElements() {
        var position = -1
        for (setting in module.settings) {
            if (setting.visibility.visibleInClickGui && setting.shouldBeVisible) run addElement@{
                position++
                if (this.menuElements.any { it.setting === setting }) return@addElement
                val newElement = when (setting) {
                    is BooleanSetting ->    ElementBoolean(this, setting)
                    is NumberSetting ->     ElementSlider(this, setting)
                    is StringSelectorSetting ->   ElementSelector(this, setting)
                    is StringSetting ->     ElementTextField(this, setting)
//                    is ColorSetting ->      ElementColor(this, setting)
                    is ActionSetting ->     ElementAction(this, setting)
                    is KeyBindSetting ->    ElementKeyBind(this, setting)
                    is DropdownSetting ->   ElementDropdown(this, setting)
                    else -> return@addElement
                }
                try { // for now ig
                    this.menuElements.add(position, newElement)
                } catch (e: IndexOutOfBoundsException) {
//                    this.menuElements.add(newElement)
                }
            } else {
                this.menuElements.removeIf { it.setting === setting }
            }
        }
    }


    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) : Double {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0)

        val colour = this.colourAnimation.get(Color.GREEN, Color(ColorUtil.outlineColor), this.module.enabled)
//        val extendOffs = this.extendAnimation.get(this.height, this.actualHeight, !this.extended)
//        val lineOffs = this.lineAnimation.get(0.0, this.width - 10.0, !this.extended)
//        drawRoundedRect(5.0, this.height - 1.0, lineOffs, 1.0, 1.0, Color.LIGHT_GRAY)
        drawOutlinedRectBorder(0.0, 0.0, this.width, this.animatedHeight, 3.0, 1.0, colour)

        FontUtil.drawStringWithShadow(module.name, 5.0, 7.0, scale = 1.4)
        if (!this.extended) this.description.forEachIndexed { i, it ->
                FontUtil.drawStringWithShadow(it, 7.0, 10.0 + fontHeight * (i + 1), Color.LIGHT_GRAY.rgb)
        }

        if (this.extended) drawRoundedRect(5.0, this.height - 1.0, this.width - 10.0, 1.0, 1.0, Color.LIGHT_GRAY)

        this.length = this.height + 5.0
        if (this.extended || this.extendAnimation.isAnimating() && this.menuElements.isNotEmpty()) {
            this.menuElements.forEach {
                it.y = this.length
                it.update()
                this.length += it.drawScreen(mouseX, mouseY, partialTicks)
            }
        }
        if (this.extended) debugMessage("${this.height + getSettingHeight()} | ${this.actualHeight}")

        GlStateManager.popMatrix()

        return this.animatedHeight + 5.0
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return when {
            this.isButtonHovered(mouseX, mouseY) -> when (mouseButton) {
                0 -> this.module.toggle().let { true }.also {
                    this.colourAnimation.start()
                }
                1 -> {
                    if (this.menuElements.isNotEmpty()) {
                        this.extended = !this.extended
                        this.extendAnimation.start()
//                        this.lineAnimation.start()
                        if (!this.extended) this.menuElements.forEach { it.listening = false }
                    }
                    true
                }
                else -> false
            }
            this.isMouseUnderButton(mouseX, mouseY) -> this.menuElements.reversed().any {
                it.mouseClicked(mouseX, mouseY, mouseButton).also { if (it) updateElements() }
            }
            else -> false
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (this.extended) this.menuElements.reversed().forEach { it.mouseReleased(mouseX, mouseY, state) }
    }

    fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (this.extended) this.menuElements.reversed().forEach { it.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick) }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (this.extended) {
            this.menuElements.reversed().forEach {
                if (it.keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    private fun isButtonHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }

    private fun isMouseUnderButton(mouseX: Int, mouseY: Int): Boolean {
        if (!this.extended) return false
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY > yAbsolute + height
    }

    private fun getSettingHeight(): Double {
        var totalHeight = 5.0
        for (i in menuElements) {
            totalHeight += i.height + 5.0
        }
        return totalHeight
    }

}