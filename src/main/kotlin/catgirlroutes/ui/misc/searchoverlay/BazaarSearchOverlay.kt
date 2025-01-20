package catgirlroutes.ui.misc.searchoverlay

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.MiscElementText
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.NeuRepo
import catgirlroutes.utils.render.HUDRenderUtils
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.toStack
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

class BazaarSearchOverlay : GuiScreen() { // todo: add ahoverlay, shit on hover, other shit

    private var x = 0.0
    private var y = 0.0

    private val overlayWidth = 305.0
    private val overlayHeight = 255.0

    private lateinit var searchBar: MiscElementText

    private var scrollOffset = 0
    private var scrollHeight = 0.0
    private var scrollY = 0.0
    private var isDraggingScroll = false

    override fun initGui() {
        val sr = ScaledResolution(mc)
        y = sr.scaledHeight / 2.0 - overlayHeight / 2.0
        x = sr.scaledWidth / 2.0 - overlayWidth / 2.0
        searchBar =  MiscElementText(x, y, overlayWidth, 20.0, radius = 3.0, bgColour = Color(ColorUtil.bgColor))
        searchBar.focus = true
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        GlStateManager.pushMatrix()

        // main box
        drawRoundedBorderedRect(x - 5.0, y - 5.0, overlayWidth + 10, overlayHeight + 10, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        drawRoundedBorderedRect(x, y + 25.0, overlayWidth, overlayHeight - 25.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        searchBar.render(mouseX, mouseY)

        searchResults.clear()
        var offset = 30.0
        if (searchBar.text.length > 2) {
            NeuRepo.items.filter { it.name.contains(searchBar.text, true) && it.bazaar }
                .forEach { item ->
                    val posY = y + offset - scrollOffset
                    val resultButton = MiscElementButton("", x + 5.0, posY, overlayWidth - 20.0, 20.0, 1.0, 3.0) {
                        commandAny("bz ${item.name.replace(Regex("\\u00A7."), "")}") // command for now
                    }
                    searchResults.add(resultButton)

                    if (posY >= y + 25.0 && posY <= y + 25.0 * 10) {
                        resultButton.render(mouseX, mouseY)
                        FontUtil.drawString(item.name, x + 25.0, posY + 7.0)
                        HUDRenderUtils.drawItemStackWithText(item.toStack(), x + 7.0, posY + 3.0)
                    }

                    offset += 25.0
                }
        } else scrollOffset = 0

        if (searchResults.isNotEmpty()) {
            scrollHeight = if (searchResults.size <= 9) overlayHeight - 35.0 else (overlayHeight - 25.0) * (9.0 / searchResults.size)
            scrollY = y + 30.0 + (scrollOffset / ((searchResults.size - 9) * 25.0)).coerceIn(0.0, 1.0) * (overlayHeight - 35.0 - scrollHeight)
            drawRoundedBorderedRect(x + overlayWidth - 10.0, scrollY, 5.0, scrollHeight, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        searchBar.mouseClicked(mouseX, mouseY, mouseButton)
        searchBar.focus = true

        if (isHoveringScroll(mouseX, mouseY) && mouseButton == 0) {
            isDraggingScroll = true
        }

        searchResults.forEach { it.mouseClicked(mouseX, mouseY, mouseButton) }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDraggingScroll) {
            val scrollPercentage = ((mouseY - (y + 30.0)) / (overlayHeight - 35.0)).coerceIn(0.0, 1.0)
            val newScrollOffset = (scrollPercentage * ((searchResults.size - 9) * 25.0)).toInt()
            scrollOffset = (newScrollOffset / 25) * 25
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        searchBar.keyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()

        var i = Mouse.getEventDWheel()
        if (i != 0) {
            if (i > 1) {
                i = 1
            }
            if (i < -1) {
                i = -1
            }

            if (this.scroll(i, Mouse.getX(), Mouse.getY())) return
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    private fun scroll(amount: Int, mouseX: Int, mouseY: Int): Boolean {
        if (searchResults.size <= 9) return false
        val diff = -amount * 25

        val realDiff = (scrollOffset + diff).coerceAtLeast(0) - scrollOffset
        if ((scrollOffset + realDiff) / 25 > searchResults.size - 9) return false
        scrollOffset += realDiff
        return true
    }

    private fun isHoveringScroll(mouseX: Int, mouseY: Int): Boolean {
        val scrollX = x + overlayWidth - 10.0
        return mouseX >= scrollX && mouseX <= scrollX + 5.0 && mouseY >= scrollY && mouseY <= scrollY + scrollHeight
    }

    companion object {
        val searchResults: ArrayList<MiscElementButton> = arrayListOf()
    }
}