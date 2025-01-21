package catgirlroutes.ui.misc.searchoverlay

import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.MiscElementBoolean
import catgirlroutes.ui.misc.elements.impl.MiscElementSelector
import catgirlroutes.ui.misc.elements.impl.MiscElementText
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.NeuRepo
import catgirlroutes.utils.render.HUDRenderUtils
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.renderRect
import catgirlroutes.utils.toStack
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.client.config.GuiUtils
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

class BazaarSearchOverlay : GuiScreen() { // todo: shit

    private var x = 0.0
    private var y = 0.0

    private val overlayWidth = 305.0
    private val overlayHeight = 255.0

    private var visibleRange = 0.0 .. 0.0

    private lateinit var searchBar: MiscElementText

    private var lastSelected = "0"
    private var starSelector = MiscElementSelector(
        "", "0",
        arrayListOf("§6➊", "§6➋", "§6➌", "§6➍", "§6➎", "§c➊", "§c➋", "§c➌", "§c➍", "§c➎"),
        width = 15.0, height = 15.0, thickness = 1.0, radius = 3.0,
        vertical = false, optionsPerRow = 5
    )
    private val forcePetLvl = MiscElementBoolean(text = "Lvl 100 Pets", radius = 3.0, thickness = 1.0)

    private var scrollOffset = 0
    private var scrollHeight = 0.0
    private var scrollY = 0.0
    private var isDraggingScroll = false

    override fun initGui() {
        val sr = ScaledResolution(mc)

        y = sr.scaledHeight / 2.0 - overlayHeight / 2.0
        x = sr.scaledWidth / 2.0 - overlayWidth / 2.0

        visibleRange = (y + 25.0)..(y + 25.0 * 10)

        searchBar = MiscElementText(x, y, overlayWidth, 20.0, radius = 3.0, bgColour = Color(ColorUtil.bgColor))
        searchBar.focus = true
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        GlStateManager.pushMatrix()

        val ahX = x + overlayWidth + 10.0
        val ahY = y + 25.0
        val ahW = 110.0
        val ahH = 85.0

        // ah shit box
        drawRoundedBorderedRect(ahX - 10.0, ahY - 5.0, ahW + 10.0, ahH + 10.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(ahX, ahY, ahW - 5.0, 55.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(ahX, ahY + 60.0, ahW - 5.0, 25.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        FontUtil.drawString("Stars:", ahX + 5.0, ahY + 5.0)
//        FontUtil.drawString("Only Lvl 100", ahX + 6.0, ahY + ahH / 2)

        starSelector.apply {
            x = ahX + 5.0
            y = ahY + 15.0
            render(mouseX, mouseY)
        }

        forcePetLvl.apply {
            x = ahX + 5.0
            y = ahY + 65.0
            render(mouseX, mouseY)
        }

        // main box
        drawRoundedBorderedRect(x - 5.0, y - 5.0, overlayWidth + 10, overlayHeight + 10, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        renderRect(ahX - 10.0 + 4.0, ahY - 5.0 + 0.5, 2.0, ahH + 10.0 - 1.0, Color(ColorUtil.bgColor))
        doAhCorners(ahX -10.0 + 4.0, ahY - 5.0, ahH + 10.0)

        // inner box
        drawRoundedBorderedRect(x, y + 25.0, overlayWidth, overlayHeight - 25.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        searchBar.prependText = if (forcePetLvl.enabled) "§7[Lvl 100]§r " else ""
        searchBar.render(mouseX, mouseY)

        searchResults.clear()
        val tooltips = mutableListOf<Pair<List<String>, Pair<Int, Int>>>()

        var offset = 30.0
        if (searchBar.text.length > 2) {
            NeuRepo.items.filter {
                it.name.contains(searchBar.text, true) && it.auction &&
                !(forcePetLvl.enabled && !it.name.contains("Lvl"))
            }
                .forEach { item ->
                    val posY = y + offset - scrollOffset
                    val resultButton = MiscElementButton("", x + 5.0, posY, overlayWidth - 20.0, 20.0, 1.0, 3.0) {
                        commandAny("bz ${item.name.replace(Regex("\\u00A7."), "")}") // command for now
                    }
                    searchResults.add(resultButton)

                    if (posY in visibleRange) {
                        val itemStack = item.toStack()
                        resultButton.render(mouseX, mouseY)
                        FontUtil.drawString(item.name, x + 25.0, posY + 7.0)
                        HUDRenderUtils.drawItemStackWithText(itemStack, x + 7.0, posY + 3.0)
                        if (resultButton.isHovered(mouseX, mouseY)) {
                            tooltips.add(Pair(itemStack.getTooltip(mc.thePlayer, false), Pair(mouseX, mouseY)))
                        }
                    }
                    offset += 25.0
                }
        } else scrollOffset = 0

        if (searchResults.isNotEmpty()) {
            scrollHeight = if (searchResults.size <= 9) overlayHeight - 35.0 else (overlayHeight - 25.0) * (9.0 / searchResults.size)
            scrollY = y + 30.0 + (scrollOffset / ((searchResults.size - 9) * 25.0)).coerceIn(0.0, 1.0) * (overlayHeight - 35.0 - scrollHeight)
            drawRoundedBorderedRect(x + overlayWidth - 10.0, scrollY, 5.0, scrollHeight, 3.0, 1.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)
        }

        tooltips.forEach { (tooltip, position) ->
            GuiUtils.drawHoveringText(
                tooltip, position.first, position.second, width, height, -1, mc.fontRendererObj
            )
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        forcePetLvl.mouseClicked(mouseX, mouseY, mouseButton)
        searchBar.mouseClicked(mouseX, mouseY, mouseButton)
        searchBar.focus = true

        if (starSelector.mouseClicked(mouseX, mouseY, mouseButton)) {
            debugMessage(starSelector.selected)
            debugMessage(this.lastSelected)
            if (starSelector.selected == this.lastSelected) {
                // starSelector.selected = blahblhablah doesn't work for some reason, so I just reset it this way...
                starSelector = MiscElementSelector(
                    "", "0",
                    arrayListOf("§6➊", "§6➋", "§6➌", "§6➍", "§6➎", "§c➊", "§c➋", "§c➌", "§c➍", "§c➎"),
                    width = 15.0, height = 15.0, thickness = 1.0, radius = 3.0,
                    vertical = false, optionsPerRow = 5
                )
                this.lastSelected = "00"
            } else {
                this.lastSelected = starSelector.selected
            }
        }

        if (isHoveringScroll(mouseX, mouseY) && mouseButton == 0) {
            isDraggingScroll = true
        }
        if (mouseY.toDouble() !in visibleRange) return
        searchResults.forEach { it.mouseClicked(mouseX, mouseY, mouseButton) }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        searchBar.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
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

            if (this.scroll(i)) return
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    private fun scroll(amount: Int): Boolean {
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

    private fun doAhCorners(x: Double, y: Double, h: Double) {
        renderRect(x + 0.5, y - 0.5, 1.0, 1.0, Color(ColorUtil.bgColor))
        renderRect(x + 1.5, y - 1.0, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(x + 0.5, y - 1.0, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(x + 1.5, y, 0.5, 0.5, ColorUtil.clickGUIColor.darker())

        renderRect(x + 0.5, y + h - 0.5, 1.0, 1.0, Color(ColorUtil.bgColor))
        renderRect(x + 1.5, y + h + 0.5, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(x + 0.5, y + h + 0.5, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
        renderRect(x + 1.5, y + h - 0.5, 0.5, 0.5, ColorUtil.clickGUIColor.darker())
    }

    companion object {
        val searchResults: ArrayList<MiscElementButton> = arrayListOf()
    }
}