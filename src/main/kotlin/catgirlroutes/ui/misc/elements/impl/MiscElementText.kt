package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.getStringWidth
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.apache.commons.lang3.StringUtils
import java.awt.Color

// modified neu shit
class MiscElementText(
    width: Double = 20.0,
    height: Double = 100.0,
    var options: Int = 0,
    var t: String = "",
) : MiscElement(width = width, height = height) {

    private var barPadding: Int = 2

    var focus: Boolean = false


    var prependText: String = ""

    private val textField: GuiTextField = GuiTextField(
        0, mc.fontRendererObj, 0,
        0, 0, 0
    )

    init {
        textField.isFocused = true
        textField.setCanLoseFocus(false)
        textField.maxStringLength = 9999
        textField.text = t
    }

    fun getText(): String {
        return textField.text
    }

    fun setText(text: String) {
        textField.text = text
    }

    fun setSize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }

    // what the fuck is this?
    override fun toString(): String {
        return textField.text
    }

    fun getElementHeight(): Double {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val paddingUnscaled: Int = barPadding / sr.scaleFactor

        val numLines: Int = StringUtils.countMatches(textField.text, "\n") + 1
        val extraSize: Double = (this.height - 8) / 2 + 8
        val bottomTextBox: Double = this.height + extraSize * (numLines - 1)

        return bottomTextBox + paddingUnscaled * 2
    }

    fun getElementWidth(): Double {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val paddingUnscaled: Int = barPadding / sr.scaleFactor

        return this.width + paddingUnscaled * 2
    }

    private fun getCursorPos(mouseX: Int, mouseY: Int): Int {
        val xComp = mouseX - x
        val yComp = mouseY - y
        val extraSize = (this.height + 8) / 2
        val renderText = prependText + textField.text

        val lineNum = (yComp / extraSize).coerceAtLeast(0.0)
        val lines = renderText.lines()

        val targetLine = lines.getOrNull(lineNum.toInt()) ?: return renderText.length

        val padding = ((5).coerceAtMost(this.width.toInt() - strLenNoColor(targetLine))) / 2
        val adjustedX = (xComp - padding).coerceAtLeast(0.0)

        val trimmed = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(targetLine, adjustedX.toInt())
        val cursorInLine = strLenNoColor(trimmed)

        val cursorIndex = lines.take(lineNum.toInt()).sumOf { it.length + 1 } + cursorInLine

        debugMessage(cursorIndex.coerceAtMost(renderText.length))
        return cursorIndex.coerceAtMost(renderText.length)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 1) {
            textField.text = ""
        } else {
            textField.cursorPosition = getCursorPos(mouseX, mouseY)
            debugMessage("AMONGUS " + textField.cursorPosition)
        }
        focus = true
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun otherComponentClick() {
        focus = false
        textField.setSelectionPos(textField.cursorPosition)
        debugMessage("ALONGUS " + textField.cursorPosition)
    }

    private fun strLenNoColor(str: String): Int {
        return str.replace("(?i)\\u00A7.".toRegex(), "").length
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (focus) {
            textField.setSelectionPos(getCursorPos(mouseX, mouseY))
            textField.cursorPosition = getCursorPos(mouseX, mouseY)
            debugMessage("AGONGUS " + textField.cursorPosition)
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean { // todo: add more ctrl things (ctrl + a/c/x)
        var typedChar2 = typedChar
        if (focus) {
            if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                textField.setEnabled(false)
                val (start, end) = listOf(textField.cursorPosition, textField.selectionEnd).sorted()

                textField.text = StringBuilder(getText()).replace(start, end, GuiScreen.getClipboardString()).toString()
                textField.cursorPosition = start + GuiScreen.getClipboardString().length
                debugMessage("AZINGUS" + textField.cursorPosition)
            } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
                debugMessage("CTRL A")
                textField.setCursorPositionEnd()
                textField.setSelectionPos(0)
            } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                GuiScreen.setClipboardString(textField.selectedText)
            }
            else textField.setEnabled(true)

            val old = textField.text
            if ((options and FORCE_CAPS) != 0) typedChar2 = typedChar2.uppercaseChar()
            if ((options and NO_SPACE) != 0 && typedChar2 == ' ') return false

            textField.apply {
                isFocused = true
                textboxKeyTyped(typedChar2, keyCode)
                if ((options and NUM_ONLY) != 0 && text.any { it !in "0-9." }) text = old
            }
        }
        return super.keyTyped(typedChar2, keyCode)
    }

    override fun render(x: Double, y: Double) {
        this.x = x
        this.y = y
        drawTextbox()
    }

    private fun drawTextbox() {
        val sr = ScaledResolution(mc)
        val renderText = prependText + textField.text
        GlStateManager.disableLighting()

        val paddingUnscaled = (barPadding / sr.scaleFactor).coerceAtLeast(1)
        val extraSize = (this.height - 8) / 2 + 8
        val bottomTextBox = y + this.height + extraSize * (StringUtils.countMatches(renderText, "\n") + 1 - 1)

        drawRoundedRect(
            x - paddingUnscaled, y - paddingUnscaled,
            (this.width + 2 * paddingUnscaled).toDouble(), bottomTextBox - y + 2 * paddingUnscaled,
            5.0, if (focus) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        drawRoundedRect(x, y, this.width.toDouble(), bottomTextBox - y, 5.0, Color(ColorUtil.buttonColor))

        val textNoColor = renderText.replace(Regex("(?i)\\u00A7([^\\u00B6\\n])(?!\\u00B6)")) { "\u00B6${it.groupValues[1]}" }

        renderText.split("\n").filter { it.isNotEmpty() }.forEachIndexed { index, text ->
            FontUtil.drawString(
                mc.fontRendererObj.trimStringToWidth(text, this.width.toInt() - 10),
                x + 5,
                y + (this.height - 8) / 2 + index * extraSize
            )
        }

        if (focus && System.currentTimeMillis() % 1000 > 500) {
            val cursorText = renderText.substring(0, textField.cursorPosition + prependText.length).split("\n")
//            debugMessage(cursorText)
//            debugMessage(textField.cursorPosition)
            val cursorX = x + 5 + (cursorText.lastOrNull()?.let { getStringWidth(it) } ?: 0)
            val cursorY = y + (this.height - 8) / 2 - 1 + StringUtils.countMatches(cursorText.joinToString("\n"), "\n") * extraSize

            drawRoundedRect(cursorX, cursorY, 1.0, 10.0, 0.0, Color.WHITE)
        }
//        debugMessage(textField.selectedText)

        if (textField.selectedText.isNotEmpty()) {
            val (left, right) = listOf(textField.cursorPosition, textField.selectionEnd).map { it + prependText.length }.let { it.minOrNull() to it.maxOrNull() }
            var texX = 0.0
            var texY = 0.0
            var bold = false
            var sectionSignPrev = false

            textNoColor.forEachIndexed { i, c -> // I think it's ctrl + a logic
                if (c == '\n') {
                    if (i in left!! until right!!) drawRoundedRect(x + 5 + texX, y + (this.height - 8) / 2 - 1 + texY, 3.0, 9.0, 5.0, Color.LIGHT_GRAY)
                    texX = 0.0; texY += extraSize; return@forEachIndexed
                }

                bold = if (sectionSignPrev) c == 'l' else bold
                sectionSignPrev = c == '\u00B6'
                val len = getStringWidth(c.toString()) + if (bold) 1 else 0

                if (i in left!! until right!!) {
                    drawRoundedRect(x + 5 + texX, y + (this.height - 8) / 2 - 1 + texY, len.toDouble(), 10.0, 5.0, Color(ColorUtil.bgColor))
                    FontUtil.drawString(c.toString(), x + 5 + texX, y + this.height / 2.0 - 4.0 + texY, Color.BLACK.rgb)
                    if (bold) FontUtil.drawString(c.toString(), x + 5 + texX + 1, y + this.height / 2.0 - 4.0 + texY, Color.BLACK.rgb)
                }
                texX += len
            }
        }

    }

    companion object { // todo: change
        const val NUM_ONLY = 0b10000
        const val NO_SPACE = 0b01000
        const val FORCE_CAPS = 0b00100
    }
}
