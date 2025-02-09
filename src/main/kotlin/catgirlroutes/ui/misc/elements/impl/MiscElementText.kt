package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.getStringWidth
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import me.odinmain.utils.noControlCodes
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * A [MiscElement] that displays and optionally allows input for a string value with customizable options and style.
 *
 * @param x The x-coordinate position (default is 0.0).
 * @param y The y-coordinate position (default is 0.0).
 * @param width The width of the element (default is 100.0).
 * @param height The height of the element (default is 20.0).
 * @param value The initial text value (default is an empty string).
 * @param options An integer representing additional options (NUM_ONLY, NO_SPACE, FORCE_CAPS).
 * @param prependText Text that will be displayed before the text (default is none).
 * @param thickness The border thickness (default is 2.0).
 * @param radius The radius of the corners (default is 5.0).
 * @param bgColour The background color of the element (default is `ColorUtil.buttonColor`).
 */
class MiscElementText( // todo: prob add undo redo in the future
    x: Double = 0.0,
    y: Double = 0.0,
    width: Double = 100.0,
    height: Double = 20.0,
    val value: String = "",
    var size: Int = 0,
    var placeholder: String = "",
    var options: Int = 0,
    var prependText: String = "",
    var thickness: Double = 2.0,
    var radius: Double = 5.0,
    var bgColour: Color = Color(ColorUtil.buttonColor),
    var outlineColour: Color = Color(ColorUtil.outlineColor),
    var outlineFocusColour: Color = ColorUtil.clickGUIColor
) : MiscElement(x, y, width, height) {

    var focus: Boolean = false
    private var scrollOffset = 0.0

    private val textField: GuiTextField = GuiTextField(
        0, mc.fontRendererObj, 0,
        0, 0, 0
    )

    var text: String
        get() = this.textField.text
        set(value) { this.textField.text = value }

    private var cursorPosition: Int
        get() = this.textField.cursorPosition
        set(value) { this.textField.cursorPosition = value }

    private val renderText get() = this.prependText + text
    private val sizeText get() = if (this.size > 0) "${renderText.length}/${this.size}" else ""
    private val fieldWidth get() = this.width - 10 - getStringWidth(sizeText)

    init {
        textField.isFocused = true
        textField.setCanLoseFocus(false)
        textField.maxStringLength = 9999
        textField.text = value
    }

    private fun updateScrollOffset() {
        val cursorX = getStringWidth(renderText.substring(0, cursorPosition + prependText.length))
        if (cursorX - scrollOffset > fieldWidth) {
            scrollOffset = cursorX - fieldWidth
        } else if (cursorX < scrollOffset) {
            scrollOffset = cursorX.toDouble()
        }
        scrollOffset = scrollOffset.coerceAtLeast(0.0)
    }


    private fun getCursorPos(mouseX: Int): Int {
        val xComp = mouseX - x - getStringWidth(prependText) - 5
        val adjustedX = (xComp + scrollOffset).coerceAtLeast(0.0)

        val trimmed = mc.fontRendererObj.trimStringToWidth(this.text, adjustedX.toInt())

        return trimmed.length.coerceAtMost(renderText.length)
    }

    private var lastClickTime: Long = 0
    private var doubleClickStart: Int = -1
    private var doubleClickEnd: Int = -1

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!isHovered(mouseX, mouseY)) {
            focus = false
            textField.setSelectionPos(cursorPosition)
            return false
        }

        // in new click gui text fields don't get unfocused if you click on another element for whatever (this.mouseClicked is processed if I click within the element or smt) reason this is an easy fix (kinda)
        // if it's still going to bother I'll actually fix it I guess
        currentlyFocused?.focus = false
        currentlyFocused?.cursorPosition?.let { currentlyFocused?.textField?.setSelectionPos(it) }
        currentlyFocused = this

        val currentTime = System.currentTimeMillis()
        val cursorPos = getCursorPos(mouseX)

        when(mouseButton) {
            0 -> {
                if (currentTime - lastClickTime < 250) {
                    val wordBounds = findWordBounds(renderText, cursorPos)
                    doubleClickStart = wordBounds.first
                    doubleClickEnd = wordBounds.second

                    cursorPosition = doubleClickStart
                    textField.setSelectionPos(doubleClickEnd)
                } else cursorPosition = cursorPos
            }
            1 -> text = ""
            else -> cursorPosition = cursorPos
        }
        focus = true
        lastClickTime = currentTime

        updateScrollOffset()
        return true
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (!focus || !this.isHovered(mouseX, mouseY)) return
        this.textField.setSelectionPos(getCursorPos(mouseX))
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!focus) return false

        val isSpecialKey = GuiScreen.isKeyComboCtrlA(keyCode) ||
                keyCode == Keyboard.KEY_BACK ||
                keyCode == Keyboard.KEY_RIGHT ||
                keyCode == Keyboard.KEY_LEFT
                GuiScreen.isCtrlKeyDown()

        if (this.size > 0 && !isSpecialKey && this.size == this.text.length) return false

        var typedChar2 = typedChar

        when {
            GuiScreen.isKeyComboCtrlV(keyCode) -> {
                textField.setEnabled(false)
                val clipboardText = GuiScreen.getClipboardString() ?: return false
                val (start, end) = listOf(this.cursorPosition, this.textField.selectionEnd).sorted()

                val pasteText = if (this.size > 0) {
                    val remainingSpace = this.size - (this.text.length - (end - start))
                    clipboardText.substring(0, minOf(clipboardText.length, remainingSpace))
                } else {
                    clipboardText
                }

                if (pasteText.isNotEmpty()) {
                    this.text = StringBuilder(this.text).replace(start, end, pasteText).toString()
                    this.cursorPosition = start + pasteText.length
                }
            }
            else -> this.textField.setEnabled(true)
        }

        val old = this.text
        if ((options and FORCE_CAPS) != 0) typedChar2 = typedChar2.uppercaseChar()
        if ((options and NO_SPACE) != 0 && typedChar2 == ' ') return false

        textField.apply {
            isFocused = true
            textboxKeyTyped(typedChar2, keyCode)
            if ((options and NUM_ONLY) != 0 && text.any { it !in "0-9." }) text = old
        }

        updateScrollOffset()
        return true
    }

    override fun render(mouseX: Int, mouseY: Int) {
        val yPos = y + (this.height - 8) / 2
        val maxScrollOffset = (getStringWidth(renderText) - this.fieldWidth).coerceAtLeast(0.0)
        scrollOffset = scrollOffset.coerceIn(0.0, maxScrollOffset)

        GlStateManager.pushMatrix()
        GlStateManager.color(1.0f, 1.0f, 1.0f)

        drawRoundedBorderedRect(x, y, this.width, this.height, this.radius, this.thickness, bgColour, if (focus) outlineFocusColour else outlineColour)

        val sizeColour = if (renderText.length == this.size) Color.RED else Color.LIGHT_GRAY
        FontUtil.drawString(sizeText, x + this.fieldWidth + 8.0, yPos, sizeColour.rgb)

        val visibleTextStartIndex = mc.fontRendererObj.trimStringToWidth(renderText, scrollOffset.toInt()).length
        val visibleText = mc.fontRendererObj.trimStringToWidth(
            renderText.substring(visibleTextStartIndex),
            this.fieldWidth.toInt() + getStringWidth(" ")
        )

        FontUtil.drawString(visibleText, x + 5, yPos)

        if (this.text.isEmpty()) FontUtil.drawString(this.placeholder, x + 5, yPos, Color.LIGHT_GRAY.rgb)

        if (focus && System.currentTimeMillis() % 1000 > 500) {
            val cursorText = renderText.substring(0, cursorPosition + prependText.length)

            val cursorWidth = (getStringWidth(cursorText) - scrollOffset).coerceIn(0.0, this.fieldWidth)
            val cursorX = x + 5 + cursorWidth

            drawRoundedRect(cursorX, yPos - 1, 1.0, 10.0, 1.0, Color.WHITE)
        }

        if (textField.selectedText.isNotEmpty()) {
            val (left, right) = listOf(cursorPosition, textField.selectionEnd)
                .map { it + prependText.noControlCodes.length }
                .let { it.minOrNull() to it.maxOrNull() }

            var texX = 0.0
            renderText.noControlCodes.forEachIndexed { i, c ->
                val len = getStringWidth(c.toString())
                if (i in left!! until right!!) {
                    val currentTexX = texX - scrollOffset

                    if (currentTexX + len > 0 && currentTexX < this.fieldWidth) {
                        drawRoundedRect(x + 5 + currentTexX, yPos - 1, len.toDouble(), 10.0, 0.1, Color.WHITE.withAlpha(150))
                    }
                }
                texX += len
            }
        }

        GlStateManager.popMatrix()
    }

    private fun findWordBounds(text: String, cursorPos: Int): Pair<Int, Int> {
        if (text.isEmpty()) return Pair(0, 0)

        var start = cursorPos
        var end = cursorPos

        // find start
        while (start > 0 && !text[start - 1].isWhitespace()) {
            start--
        }

        // find end
        while (end < text.length && !text[end].isWhitespace()) {
            end++
        }

        return Pair(start, end)
    }

    companion object { // todo: change
        const val NUM_ONLY = 0b10000
        const val NO_SPACE = 0b01000
        const val FORCE_CAPS = 0b00100
    }
}
