package catgirlroutes.ui.clickguinew

import catgirlroutes.CatgirlRoutes.Companion.moduleConfig
import catgirlroutes.module.Category
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.ui.clickgui.ClickGUI.Companion.panels
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.MiscElementText
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import catgirlroutes.utils.render.StencilUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

class ClickGUI : GuiScreen() {
    var scale = 2.0

    var x: Double = 0.0
    var y: Double = 0.0

    val guiWidth = 605.0
    val guiHeight = 305.0

    val categoryWidth = 105.0

    var selectedWindow: Window? = null

    private val searchBar = MiscElementText(
        width = guiWidth - categoryWidth - 5.0,
        height = 20.0,
        radius = 3.0,
        bgColour = Color(ColorUtil.bgColor)
    )
    private val categoryButtons: ArrayList<MiscElementButton> = ArrayList()

    init {

    }

    override fun initGui() {
        val sr = ScaledResolution(mc)
        x = sr.scaledWidth_double / 2.0 - guiWidth / 2.0
        y = sr.scaledHeight_double / 2.0 - guiHeight / 2.0

        searchBar.x = this.x + categoryWidth + 5.0
        searchBar.y = this.y

        windows = ArrayList()
        for (category in Category.entries) {
            windows.add(Window(category, this))
        }

        if (selectedWindow == null) {
            selectedWindow = windows[0]
        }

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        GlStateManager.pushMatrix()
        var categoryOffset = 25.0

        drawRoundedBorderedRect(x - 5.0, y - 5.0, guiWidth + 10.0, guiHeight + 10.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(x, y, categoryWidth, guiHeight, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        drawRoundedBorderedRect(x + categoryWidth + 5.0, y + 25.0, guiWidth - categoryWidth - 5.0, guiHeight - 25.0, 3.0, 2.0, Color(ColorUtil.bgColor), ColorUtil.clickGUIColor)

        val titleWidth = FontUtil.getStringWidth(ClickGui.clientName.text)
        FontUtil.drawStringWithShadow(ClickGui.clientName.text, x + categoryWidth / 2.0 - titleWidth / 2.0, y + 6.0)

        this.searchBar.render(mouseX, mouseY)
//        debugMessage(selectedWindow.category.name)
        categoryButtons.clear()
        for (window in windows) {

            val offset: Double = if (window.category == Category.SETTINGS) guiHeight - 20.0 else categoryOffset

            val categoryButton = MiscElementButton(
                "",
                x + 5.0,
                y + offset + 2.0,
                categoryWidth - 9.0,
                13.0,
                1.0,
                3.0,
                outlineColour = Color.WHITE.withAlpha(0)
            ) { this.selectedWindow = window }
            categoryButton.render(mouseX, mouseY)
            categoryButtons.add(categoryButton)

            StencilUtils.write(false)
            drawRoundedRect(x + categoryWidth + 5.0, y + 26.0, guiWidth - categoryWidth - 5.0, guiHeight - 27.0, 3.0, Color.WHITE)
            StencilUtils.erase(true)

            window.drawScreen(mouseX, mouseY, partialTicks)
            StencilUtils.dispose()

            if (this.selectedWindow == window) {
                drawRoundedBorderedRect(x + 5.0, y + offset + 2.0, categoryWidth - 9.0, 13.0, 3.0, 1.0, ColorUtil.clickGUIColor, ColorUtil.clickGUIColor)
            }
            FontUtil.drawStringWithShadow(window.category.name.capitalizeOnlyFirst(), x + 10.0, y + offset + 4.0)

            if (window.category != Category.SETTINGS) categoryOffset += 14.0
        }

        GlStateManager.popMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        val scaledMouseX = Mouse.getX() / 2
        val scaledMouseY = Mouse.getY() / 2

        var i = Mouse.getEventDWheel()
        if (i != 0) {
            if (i > 1) {
                i = 1
            }
            if (i < -1) {
                i = -1
            }
            if (isShiftKeyDown()) {
                i *= 7
            }
            debugMessage(scaledMouseX)
            debugMessage(scaledMouseY)

            for (window in windows.reversed()) {
                if (window.scroll(i, scaledMouseX, scaledMouseY)) return
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        this.searchBar.mouseClicked(mouseX, mouseY, mouseButton)
        categoryButtons.forEach { it.mouseClicked(mouseX, mouseY, mouseButton) }

        windows.reversed().forEach { if (it.mouseClicked(mouseX, mouseY, mouseButton)) return }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        windows.reversed().forEach { it.mouseReleased(mouseX, mouseY, state) }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        windows.reversed().forEach { if (it.keyTyped(typedChar, keyCode)) return }
//        if (keyCode == ClickGui.settings.last().value && System.currentTimeMillis() - openedTime > 200) {
//            mc.displayGuiScreen(null as GuiScreen?)
//            if (mc.currentScreen == null) {
//                mc.setIngameFocus()
//            }
//            return
//        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        windows.reversed().forEach { it.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick) }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun onGuiClosed() {
        moduleConfig.saveConfig()
    }

    companion object {
        const val CLICK_GUI_SCALE = 2.0
        var windows: ArrayList<Window> = arrayListOf()
    }
}