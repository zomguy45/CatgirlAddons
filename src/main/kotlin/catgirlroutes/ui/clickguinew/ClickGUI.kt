package catgirlroutes.ui.clickguinew

import catgirlroutes.CatgirlRoutes.Companion.moduleConfig
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.ui.Screen
import catgirlroutes.ui.clickgui.util.Alignment
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.ColorUtil.withAlpha
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.capitalizeOnlyFirst
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseX
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseY
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.button
import catgirlroutes.ui.misc.elements.impl.textField
import catgirlroutes.ui.misc.elements.util.update
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.resetScissor
import catgirlroutes.utils.render.HUDRenderUtils.scissor
import org.lwjgl.input.Keyboard
import java.awt.Color

class ClickGUI : Screen() { // todo: module description // TODO RECODE

    val guiWidth = 355.0
    val guiHeight = 260.0

    var x = 0.0
    var y = 0.0

    val categoryWidth = 85.0

    var selectedWindow: Window

    val searchBar = textField {
        size(guiWidth - categoryWidth - 5.0, 20.0)
        placeholder = "Search..."
        colour = ColorUtil.bgColor
        thickness = 2.0
    }

    private val categoryButtons: ArrayList<MiscElementButton> = ArrayList()

    init {
        windows = ArrayList()
        for (category in Category.entries) {
            windows.add(Window(category, this))
        }

        this.selectedWindow = windows[1] // render
    }

    override fun onInit() {
        x = getX(guiWidth)
        y = getY(guiHeight)

        searchBar.x = this.x + categoryWidth + 5.0
        searchBar.y = this.y

        windows.forEach {
            it.x = x + categoryWidth + 10.0
            it.y = y + 25.0 + 5.0
        }
    }

    override fun draw() {
        var categoryOffset = 25.0

        if (ClickGui.showUsageInfo) renderUsage()

        drawRoundedBorderedRect(x - 5.0, y - 5.0, guiWidth + 10.0, guiHeight + 10.0, 3.0, 2.0, ColorUtil.bgColor, ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(x, y, categoryWidth, guiHeight, 3.0, 2.0, ColorUtil.bgColor, ColorUtil.clickGUIColor)

        drawRoundedBorderedRect(x + categoryWidth + 5.0, y + 25.0, guiWidth - categoryWidth - 5.0, guiHeight - 25.0, 3.0, 2.0, ColorUtil.bgColor, ColorUtil.clickGUIColor)

        val titleWidth = FontUtil.getStringWidth(ClickGui.guiName)
        FontUtil.drawStringWithShadow(ClickGui.guiName, x + categoryWidth / 2.0 - titleWidth / 2.0, y + 6.0)

        this.searchBar.update { // FIXME
            outlineColour = ColorUtil.outlineColor
            outlineHoverColour = ColorUtil.clickGUIColor
        }.draw(mouseX, mouseY)

        categoryButtons.clear()

        scissor(x, y + 26.0, guiWidth, guiHeight - 27.0)
        for (window in windows) {

            val offset: Double = if (window.category == Category.SETTINGS) guiHeight - 20.0 else categoryOffset

            val categoryButton = button {
                text = window.category.name.capitalizeOnlyFirst()
                at(x + 5.0, y + offset + 2.0)
                size(categoryWidth - 9.0, 14.0)
                textShadow = true
                colour = Color.WHITE.withAlpha(0)
                hoverColour = colour
                outlineColour = Color.WHITE.withAlpha(0)
                outlineHoverColour = Color.WHITE.withAlpha(0)
                textPadding = 12.0
                alignment = Alignment.LEFT
                onClick { selectedWindow = window }
            }

            if (this.searchBar.text.isNotEmpty()) {
                val containsSearch = window.moduleButtons.any { it.module.name.contains(this.searchBar.text, true) }
                if (!containsSearch) {
                    continue
                }
            }

            categoryButtons.add(categoryButton)

            window.draw()

            if (this.selectedWindow == window) {
                drawRoundedBorderedRect(x + 5.0, y + offset + 2.0, categoryWidth - 9.0, 14.0, 3.0, 1.0, ColorUtil.outlineColor, ColorUtil.outlineColor)
            }
            categoryButton.draw(mouseX, mouseY)

            if (window.category != Category.SETTINGS) categoryOffset += 14.0
        }
        resetScissor()
    }

    override fun onScroll(amount: Int) {
        for (window in windows.reversed()) {
            if (window.scroll(amount)) return
        }
        this.selectedWindow.moduleButtons.forEach { if (it.scroll(amount)) return }
    }

    override fun onMouseClick(mouseButton: Int) {
        this.searchBar.onMouseClick(mouseX, mouseY, mouseButton)
        categoryButtons.firstOrNull { it.onMouseClick(mouseX, mouseY, mouseButton) }?.let {
            selectedWindow.moduleButtons.forEach { moduleButton -> moduleButton.extended = false }
        }
        windows.reversed().forEach { if (it.mouseClicked(mouseButton)) return }
    }

    override fun onMouseRelease(state: Int) {
        windows.reversed().forEach { it.mouseReleased(state) }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (this.searchBar.onKey(typedChar, keyCode)) return
        windows.reversed().forEach { if (it.keyTyped(typedChar, keyCode)) return }
        when (keyCode) {
            Keyboard.KEY_F -> if (isCtrlKeyDown()) this.searchBar.isFocused = true
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun onMouseClickMove(mouseButton: Int, timeSinceLastClick: Long) {
        windows.reversed().forEach { it.mouseClickMove(mouseButton, timeSinceLastClick) }
        searchBar.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick)
    }

    override fun onGuiClosed() {
        windows.reversed().forEach { it.onGuiClosed() }
        this.searchBar.isFocused = false
        moduleConfig.saveConfig()
    }

    private fun renderUsage() {
        val lines = listOf("GUI Usage:",
            "Left click Module Buttons to toggle the Module.",
            "Right click Module Buttons to open settings.",
            "Middle click or Shift click Module Buttons to change the Module key bind.",
            "Disable this Overlay in the Click Gui Module in the Render Category.",
            "You can change Click GUI style in settings"
        )

        lines.forEachIndexed { i, text ->
            FontUtil.drawString(
                text,
                10.0,
                10.0 + FontUtil.getFontHeight(1.3).toDouble() * i,
                ColorUtil.clickGUIColor.rgb,
                scale = 1.3
            )
        }
    }

    fun openModule(module: Module) {
        selectedWindow = windows.first { it.category == module.category }
        selectedWindow.moduleButtons.first { it.module.name == module.name }.extended = true
    }

    companion object {
        var windows: ArrayList<Window> = arrayListOf()
        var scale = 2.0
    }
}