package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.module.settings.impl.OrderSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.drawString
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.ui.clickgui.util.FontUtil.getWidth
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.button
import catgirlroutes.ui.misc.elements.util.update
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedOutline
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import java.awt.Color
import java.util.*
import kotlin.math.ceil
import kotlin.math.hypot

class ElementOrder(parent: ModuleButton, setting: OrderSetting) :
    Element<OrderSetting>(parent, setting, ElementType.ORDER) {

    private val placeholders = setting.placeholders.toList()
    private val values = setting.options.toMutableList()
    private val buttons = mutableListOf<MiscElementButton>()
    private var draggedButton: MiscElementButton? = null
    private var hoveredButton: MiscElementButton? = null
    private var offsetX = 0.0
    private var offsetY = 0.0

    val elementsHeight: Double get() {
        val numRows = ceil(setting.value.size.toDouble() / ELEMENTS_PER_ROW).toInt()
        return (numRows * ELEMENT_HEIGHT) + ((numRows - 1) * 5.0)
    }

    init {
        createButtons()
    }

    private fun createButtons() {
        buttons.clear()
        values.forEachIndexed { i, value ->
            val pos = getPosIndex(i)
            buttons.add(button {
                text = value
                at(pos.first, pos.second)
                size(ELEMENT_WIDTH, ELEMENT_HEIGHT)
                colour = Color(0, 0, 0, 128)
                outlineColour = Color(0, 0, 0, 0)
                outlineHoverColour = Color(136, 255, 136, 128)
            })
        }
    }


    override fun renderElement(): Double {

        drawString(displayName, 0.0, 0.0)
        drawRoundedOutline(0.0, fontHeight + 3.0, width, elementsHeight + 10.0, 3.0, 1.0, ColorUtil.clickGUIColor)

        buttons.forEach { button ->
            if (button != draggedButton && button != hoveredButton) {
                val (origX, origY) = getPos(button)
                button.update {
                    x = origX
                    y = origY
                }.render(mouseXRel, mouseYRel)
                drawPlaceholder(button)
            }
        }

        hoveredButton?.let { hovered ->
            hovered.render(mouseXRel, mouseYRel)
            drawPlaceholder(hovered)
        }

        draggedButton?.let { dragged ->

            hoveredButton?.let { hovered ->
                val (xPos, yPos) = getPos(dragged)

                hovered.update {
                    x = xPos
                    y = yPos
                }.render(mouseXRel, mouseYRel)

                drawPlaceholder(dragged, xPos, yPos)
            }

            val pos = getPos(dragged)
            drawRoundedRect(pos.first, pos.second, ELEMENT_WIDTH, ELEMENT_HEIGHT, 3.0, Color(136, 136, 136, 128))
            drawPlaceholder(dragged)

            dragged.update {
                colour = Color(136, 255, 136, 128)
                x = mouseXRel - offsetX
                y = mouseYRel - offsetY
            }.render(mouseXRel, mouseYRel)
        }
        return fontHeight + 3.0 + elementsHeight + 10.0
    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        buttons.forEach { button ->
            if (button.mouseClicked(mouseXRel, mouseYRel, mouseButton)) {
                draggedButton = button
                offsetX = mouseXRel - button.x
                offsetY = mouseYRel - button.y
                return true
            }
        }
        return false
    }

    override fun mouseClickMove(mouseButton: Int, timeSinceLastClick: Long) {
        if (draggedButton != null) hoveredButton = getHovered(mouseXRel, mouseYRel)
    }

    override fun mouseReleased(state: Int) {
        draggedButton?.let { dragged ->
            hoveredButton?.let { hovered ->
                val draggedIndex = buttons.indexOf(dragged)
                val hoveredIndex = buttons.indexOf(hovered)
                Collections.swap(values, draggedIndex, hoveredIndex)
                Collections.swap(buttons, draggedIndex, hoveredIndex)
            }

            buttons.forEach { button ->
                getPos(button).let { (x, y) ->
                    button.x = x
                    button.y = y
                }
            }

            dragged.colour = Color(0, 0, 0, 128)
        }

        draggedButton = null
        hoveredButton = null
    }

    private fun drawPlaceholder(button: MiscElementButton, x: Double? = null, y: Double? = null) {
        val i = buttons.indexOf(button)
        val (xPos, yPos) = if (x == null || y == null) getPos(button) else x to y
        val placeholder = placeholders[i]

        FontUtil.drawStringWithShadow(
            placeholder,
            xPos + (ELEMENT_WIDTH - placeholder.getWidth()) / 2,
            yPos + ELEMENT_HEIGHT - 10,
            Color(170, 170, 170).rgb
        )
    }

    private fun getPos(button: MiscElementButton): Pair<Double, Double> {
        return getPosIndex(buttons.indexOf(button))
    }

    private fun getPosIndex(i: Int): Pair<Double, Double> {
        val row = i / ELEMENTS_PER_ROW
        val col = i % ELEMENTS_PER_ROW
        return x + 5.0 + col * (ELEMENT_WIDTH + PADDING) to y + fontHeight + 3.0 + 5.0 + row * (ELEMENT_HEIGHT + PADDING)
    }

    private fun getHovered(mouseX: Int, mouseY: Int): MiscElementButton? {
        var closestButton: MiscElementButton? = null
        var minDistance = SNAP_THRESHOLD

        buttons.forEach { button ->
            if (button != draggedButton) {
                val (xPos, yPos) = getPos(button)
                val distance = hypot(mouseX - (xPos + ELEMENT_WIDTH / 2), mouseY - (yPos + ELEMENT_HEIGHT / 2))

                if (distance < minDistance) {
                    minDistance = distance
                    closestButton = button
                }
            }
        }

        return closestButton
    }

    companion object {
        private const val ELEMENTS_PER_ROW = 2
        private const val ELEMENT_WIDTH = 70.0
        private const val ELEMENT_HEIGHT = 30.0
        private const val PADDING = 5.0
        private const val SNAP_THRESHOLD = 20.0
    }
}