package catgirlroutes.ui.clickguinew.elements

import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.*
import catgirlroutes.ui.animations.impl.ColorAnimation
import catgirlroutes.ui.animations.impl.EaseOutQuadAnimation
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.ColorUtil.invert
import catgirlroutes.ui.clickgui.util.ColorUtil.mix
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickguinew.Window
import catgirlroutes.ui.clickguinew.elements.menu.*
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.render.HUDRenderUtils.drawOutlinedRectBorder
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import catgirlroutes.utils.render.StencilUtils
import catgirlroutes.utils.wrapText
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class ModuleButton(val module: Module, val window: Window) {
    private val menuElements: ArrayList<Element<*>> = ArrayList()

    var x = 0.0
    var y = 0.0

    val width = this.window.width / 2
    val height: Double = 25.0

    private val description: List<String> = wrapText(this.module.description, this.width - 50.0)
    private val descHeight = fontHeight * this.description.size.toDouble()

    val elementsHeight get() = this.menuElements.sumOf { it.height + 5.0 }
    private val animatedHeight get() = this.height + this.extendAnimation.get(0.0, this.extraHeight + 5.0, !this.extended)
    private val extraHeight get() = this.extraHeightAnimation.get(this.prevHeight, this.elementsHeight, this.prevHeight == this.elementsHeight)
    private val totalHeight get() = this.height + this.extendAnimation.get(0.0, this.descHeight, this.extended)
    var prevHeight = elementsHeight

    var extended = false

    val xAbsolute: Double
        get() = x + window.x
    val yAbsolute: Double
        get() = y + window.y

    val extendAnimation = EaseOutQuadAnimation(500)
    private val lineAnimation = EaseOutQuadAnimation(750)
    private val colourAnimation = ColorAnimation(100)
    val extraHeightAnimation = EaseOutQuadAnimation(500)


    init {
        updateElements()
    }

    fun updateElements() {
        val oldHeight = elementsHeight
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
                    is ColorSetting ->      ElementColor(this, setting)
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

        if (elementsHeight != oldHeight) {
            this.extraHeightAnimation.start()
            debugMessage("height: $oldHeight -> $elementsHeight")
            this.prevHeight = oldHeight
        }
    }

    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) : Double {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0.0)

        val colour = this.colourAnimation.get(ColorUtil.clickGUIColor.invert.mix(Color.GREEN), Color(ColorUtil.outlineColor), this.module.enabled)
        drawOutlinedRectBorder(0.0, 0.0, this.width, this.animatedHeight + this.totalHeight - this.height, 3.0, 1.0, colour)
        FontUtil.drawStringWithShadow(module.name, 5.0, 7.0, scale = 1.4)

        val descVisibility = extendAnimation.get(0.0, 1.0, this.extended)
        if (descVisibility > 0.0) {
            val descColour = Color.LIGHT_GRAY.withAlpha(descVisibility.coerceAtLeast(0.04).toFloat()).rgb // 0.04f is min otherwise text is rendered with alpha 1.0f (mc is weird)
            val descOffset = (1.0 - descVisibility) * 10.0
            this.description.forEachIndexed { i, it ->
                GlStateManager.enableBlend()
                FontUtil.drawStringWithShadow(it, 7.0, 10.0 + fontHeight * (i + 1) + descOffset, descColour)
                GlStateManager.disableBlend()
            }
        }

        val lineWidth = this.lineAnimation.get(0.0, this.width - 10.0, !this.extended)
        val alpha = this.lineAnimation.get(0.0, 1.0, !this.extended).toFloat()
        val lineColor = Color.LIGHT_GRAY.withAlpha(alpha)
        drawRoundedRect(5.0, this.height - 1.0, lineWidth, 1.0, 1.0, lineColor)

        val offset = this.animatedHeight + this.extendAnimation.get(0.0, this.descHeight, this.extended)

        if (this.extendAnimation.isAnimating() && this.extended || this.menuElements.isNotEmpty()) {
            StencilUtils.write(false) // no fucking clue why scissors don't work here (they just override each other for no fucking reason (same with stencil :sob:))
            drawRoundedRect(0.0, 0.0, this.width, offset, 3.0, Color.WHITE)
            StencilUtils.erase(true)

            var drawY = this.totalHeight + 5.0
            this.menuElements.forEach {
                it.y = drawY
                it.update()
                drawY += it.drawScreen(mouseX, mouseY, partialTicks)
            }
            StencilUtils.dispose()
        }

        GlStateManager.popMatrix()

        return offset + 5.0
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return when {
            isButtonHovered(mouseX, mouseY) -> when (mouseButton) {
                0 -> {
                    this.module.toggle()
                    this.colourAnimation.start()
                    true
                }
                1 -> this.menuElements.takeIf { it.isNotEmpty() }?.let { elements ->
                    if (this.extendAnimation.start()) {
                        this.extended = !this.extended
                        this.lineAnimation.start(true)
                    }
                    if (!this.extended) elements.forEach {
                        it.listening = false
                        if (it is ElementSelector || (it is ElementColor && it.setting.collapsible)) it.extended = false
                    }
                    true
                } ?: false
                else -> false
            }
            this.isMouseUnderButton(mouseX, mouseY) -> this.menuElements.reversed().any {
                it.mouseClicked(mouseX, mouseY, mouseButton).also { clicked -> if (clicked) updateElements() }
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
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + this.height + if (!extended) this.descHeight else 0.0
    }

    private fun isMouseUnderButton(mouseX: Int, mouseY: Int): Boolean {
        if (!this.extended) return false
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY > yAbsolute + this.height + if (!extended) this.descHeight else 0.0
    }
}