package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.getStringWidth
import catgirlroutes.ui.misc.elements.MiscElement
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
    width: Int = 20,
    height: Int = 100,
    options: Int = 0,
    text: String = ""
) : MiscElement() {

    private var barWidth: Int = width
    private var barHeight: Int = height
    private var barPadding: Int = 2

    private var options: Int

    var focus: Boolean = false

    private var x: Int = 0
    private var y: Int = 0

    var prependText: String = ""

    private val textField: GuiTextField = GuiTextField(
        0, mc.fontRendererObj, 0,
        0, 0, 0
    )

    init {
        textField.isFocused = true
        textField.setCanLoseFocus(false)
        textField.maxStringLength = 9999
        textField.text = text
        this.options = options
    }

    fun getText(): String {
        return textField.text
    }

    fun setText(text: String) {
        textField.text = text
    }

    fun setSize(width: Int, height: Int) {
        barWidth = width
        barHeight = height
    }

    // what the fuck is this?
    override fun toString(): String {
        return textField.text
    }

    override fun getHeight(): Int {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val paddingUnscaled: Int = barPadding / sr.scaleFactor

        val numLines: Int = StringUtils.countMatches(textField.text, "\n") + 1
        val extraSize: Int = (barHeight - 8) / 2 + 8
        val bottomTextBox: Int = barHeight + extraSize * (numLines - 1)

        return bottomTextBox + paddingUnscaled * 2
    }

    override fun getWidth(): Int {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val paddingUnscaled: Int = barPadding / sr.scaleFactor

        return barWidth + paddingUnscaled * 2
    }

    private fun getCursorPos(mouseX: Int, mouseY: Int): Int {
        val xComp = mouseX - x
        val yComp = mouseY - y
        val extraSize = (barHeight + 8) / 2
        val renderText = prependText + textField.text
        val lineNum = (yComp - extraSize) / extraSize

        val textNoColour = renderText.replace(Regex("(?i)\\u00A7([^\\u00B6])(?!\\u00B6)"), "\u00B6$1")

        var currentLine = 0
        val cursorIndex = textNoColour.indexOfFirst {
            if (it == '\n') currentLine++
            currentLine > lineNum
        }.takeIf { it != -1 } ?: textNoColour.length

        val line = renderText.substring(cursorIndex).lines().firstOrNull() ?: return 0
        val padding = (5.coerceAtMost(barWidth - strLenNoColor(line))) / 2
        val trimmed = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(line, xComp - padding)
        var linePos = strLenNoColor(trimmed)

        if (linePos < strLenNoColor(line)) {
            val after = line[linePos]
            val trimmedWidth = getStringWidth(trimmed)
            if (trimmedWidth + Minecraft.getMinecraft().fontRendererObj.getCharWidth(after) / 2 < xComp - padding) {
                linePos++
            }
        }

        return cursorIndex + linePos
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 1) {
            textField.text = ""
        } else {
            textField.cursorPosition = getCursorPos(mouseX, mouseY)
        }
        focus = true
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun otherComponentClick() {
        focus = false
        textField.setSelectionPos(textField.cursorPosition)
    }

    private fun strLenNoColor(str: String): Int {
        return str.replace("(?i)\\u00A7.".toRegex(), "").length
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (focus) {
            textField.setSelectionPos(getCursorPos(mouseX, mouseY))
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

            } else textField.setEnabled(true)

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

    override fun render(x: Int, y: Int) {
        this.x = x
        this.y = y
        drawTextbox(x.toDouble(), y.toDouble(), barWidth, barHeight, barPadding, textField, focus)
    }

    private fun drawTextbox(
        x: Double, y: Double, barSizeX: Int, barSizeY: Int, barPadding: Int,
        textField: GuiTextField, focus: Boolean
    ) {
        val sr = ScaledResolution(mc)
        val renderText = prependText + textField.text
        GlStateManager.disableLighting()

        val paddingUnscaled = (barPadding / sr.scaleFactor).coerceAtLeast(1)
        val extraSize = (barSizeY - 8) / 2 + 8
        val bottomTextBox = y + barSizeY + extraSize * (StringUtils.countMatches(renderText, "\n") + 1 - 1)

        drawRoundedRect(
            x - paddingUnscaled, y - paddingUnscaled,
            (barSizeX + 2 * paddingUnscaled).toDouble(), bottomTextBox - y + 2 * paddingUnscaled,
            5.0, if (focus) ColorUtil.clickGUIColor else Color(ColorUtil.outlineColor)
        )
        drawRoundedRect(x, y, barSizeX.toDouble(), bottomTextBox - y, 5.0, Color(ColorUtil.buttonColor))

        val textNoColor = renderText.replace(Regex("(?i)\\u00A7([^\\u00B6\\n])(?!\\u00B6)")) { "\u00B6${it.groupValues[1]}" }

        renderText.split("\n").filter { it.isNotEmpty() }.forEachIndexed { index, text ->
            FontUtil.drawString(
                mc.fontRendererObj.trimStringToWidth(text, barSizeX - 10),
                x + 5,
                y + (barSizeY - 8) / 2 + index * extraSize
            )
        }

        if (focus && System.currentTimeMillis() % 1000 > 500) {
            val cursorText = renderText.substring(0, textField.cursorPosition + prependText.length).split("\n")
            val cursorX = x + 5 + (cursorText.lastOrNull()?.let { getStringWidth(it) } ?: 0)
            val cursorY = y + (barSizeY - 8) / 2 - 1 + StringUtils.countMatches(cursorText.joinToString("\n"), "\n") * extraSize

            drawRoundedRect(cursorX, cursorY, 1.0, 10.0, 0.0, Color.WHITE)
        }

        if (textField.selectedText.isNotEmpty()) {
            val (left, right) = listOf(textField.cursorPosition, textField.selectionEnd).map { it + prependText.length }.let { it.minOrNull() to it.maxOrNull() }
            var texX = 0.0
            var texY = 0.0
            var bold = false
            var sectionSignPrev = false

            textNoColor.forEachIndexed { i, c -> // I think it's ctrl + a logic
                if (c == '\n') {
                    if (i in left!! until right!!) drawRoundedRect(x + 5 + texX, y + (barSizeY - 8) / 2 - 1 + texY, 3.0, 9.0, 5.0, Color.LIGHT_GRAY)
                    texX = 0.0; texY += extraSize; return@forEachIndexed
                }

                bold = if (sectionSignPrev) c == 'l' else bold
                sectionSignPrev = c == '\u00B6'
                val len = getStringWidth(c.toString()) + if (bold) 1 else 0

                if (i in left!! until right!!) {
                    drawRoundedRect(x + 5 + texX, y + (barSizeY - 8) / 2 - 1 + texY, len.toDouble(), 10.0, 5.0, Color(ColorUtil.bgColor))
                    FontUtil.drawString(c.toString(), x + 5 + texX, y + barSizeY / 2.0 - 4.0 + texY, Color.BLACK.rgb)
                    if (bold) FontUtil.drawString(c.toString(), x + 5 + texX + 1, y + barSizeY / 2.0 - 4.0 + texY, Color.BLACK.rgb)
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
