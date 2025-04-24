package catgirlroutes.ui.misc.searchoverlay

import catgirlroutes.CatgirlRoutes.Companion.moduleConfig
import catgirlroutes.ui.Screen
import catgirlroutes.ui.animations.impl.LinearAnimation
import catgirlroutes.ui.clickgui.util.Alignment
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseX
import catgirlroutes.ui.clickgui.util.MouseUtils.mouseY
import catgirlroutes.ui.clickguinew.Window.Companion.SCROLL_DISTANCE
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.button
import catgirlroutes.ui.misc.elements.impl.textField
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.NeuRepo
import catgirlroutes.utils.NeuRepo.toStack
import catgirlroutes.utils.RepoItem
import catgirlroutes.utils.getTooltip
import catgirlroutes.utils.noControlCodes
import catgirlroutes.utils.render.HUDRenderUtils
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawItemStackWithText
import catgirlroutes.utils.render.StencilUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.client.C12PacketUpdateSign
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.ChatComponentText
import org.lwjgl.input.Keyboard

abstract class SearchOverlay(
    val sign: TileEntitySign? = null
) : Screen() {

    val guiWidth = 305.0
    val guiHeight = 255.0

    val x = getX(guiWidth)
    val y = getY(guiHeight)

    private val visibleRange = (y + 25.0)..(y + 25.0 * 10)

    val searchBar = textField {
        at(x, y)
        size(guiWidth, 20.0)
        colour = ColorUtil.elementColor
        thickness = 2.0
        isFocused = true
    }

    private val resultButtons = mutableListOf<MiscElementButton>()

    private var scrollTarget = 0.0
    private var scrollOffset = 0.0
    private val scrollAnimation = LinearAnimation<Double>(200)

    abstract var commandPrefix: String

    abstract var history: MutableList<String>
        protected set

    init {
        sign?.let {
            it.setEditable(false)
            it.signText[0] = ChatComponentText(searchBar.textNoCodes)
        }
    }

    protected open fun drawScreenExtra(mouseX: Int, mouseY: Int) {  }
    protected open fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {  }
    protected open fun keyTypedExtra(typedChar: Char, keyCode: Int) {  }

    override fun draw() {
        GlStateManager.pushMatrix()
        scrollOffset = scrollAnimation.get(scrollOffset, scrollTarget)

        drawRoundedBorderedRect(x - 5.0, y - 5.0, guiWidth + 10.0, guiHeight + 10.0, 3.0, 2.0,ColorUtil.bgColor, ColorUtil.clickGUIColor)
        drawRoundedBorderedRect(x, y + 25.0,
            guiWidth, guiHeight - 25.0, 3.0, 2.0,ColorUtil.bgColor, ColorUtil.clickGUIColor)

        searchBar.render(mouseX, mouseY)
        drawScreenExtra(mouseX, mouseY)

        StencilUtils.write(false)
        drawRoundedBorderedRect(x, y + 26.0,
            guiWidth, guiHeight - 27.0, 3.0, 2.0,ColorUtil.bgColor, ColorUtil.clickGUIColor)
        StencilUtils.erase(true)
        renderResults()
        StencilUtils.dispose()

        GlStateManager.popMatrix()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        keyTypedExtra(typedChar, keyCode)
        searchBar.keyTyped(typedChar, keyCode)

        when (keyCode) {
            Keyboard.KEY_RETURN -> {
                history = history.addToHistory(searchBar.text)
                debugMessage("ADDING")
                if (sign != null) {
                    sign.markDirty()
                    mc.displayGuiScreen(null)
                } else {
                    mc.displayGuiScreen(null)
                    commandAny("$commandPrefix ${searchBar.textNoCodes}")
                }
            }
            Keyboard.KEY_ESCAPE -> mc.displayGuiScreen(null)
        }
        sign?.let { it.signText[0] = ChatComponentText(searchBar.textNoCodes) }
        super.keyTyped(typedChar, keyCode)
    }

    override fun onMouseClick(mouseButton: Int) {
        mouseClickedExtra(mouseX, mouseY, mouseButton)
        searchBar.mouseClicked(mouseX, mouseY, mouseButton)
        searchBar.isFocused = true

        if (mouseY.toDouble() !in visibleRange) return
        resultButtons.forEach { it.mouseClicked(mouseX, mouseY, mouseButton) }
    }

    override fun onScroll(amount: Int) {
        val h = resultButtons.size * 25.0 + 5.0
        if (h < guiHeight - 25.0) return
        scrollTarget = (scrollTarget + amount * SCROLL_DISTANCE).coerceIn(-h + guiHeight - 25.0, 0.0)
        scrollAnimation.start(true)
    }

    override fun onGuiClosed() {
        sign?.let {
            mc.netHandler?.addToSendQueue(C12PacketUpdateSign(sign.pos, sign.signText))
            sign.setEditable(true)
        }
        history = history.distinct().take(9).toMutableList()
        moduleConfig.saveConfig()
    }

    protected open fun renderResults() {
        resultButtons.clear()
        var offsetY = scrollOffset + y + 30.0

        val entries: List<Pair<String, RepoItem?>> =
            if (searchBar.textNoCodes.length > 2) {
                filterItems(searchBar.text).map { item ->
                    Pair(item.name, item)
                }
            } else {
                history.filter { it.isNotBlank() }.map {
                    if (it.startsWith("REPOITEM:")) {
                        val item = NeuRepo.getItemFromName(it.removePrefix("REPOITEM:"), false)
                        item?.let { Pair(item.name, item) } ?: Pair(it, null)
                    } else {
                        Pair(it, null)
                    }
                }
            }

        entries.forEach { (displayText, item) ->
            val stack = item?.toStack(true)
            val btn = button {
                at(x + 5.0, offsetY)
                size(guiWidth - 10.0, 20.0)
                text = displayText
                colour = ColorUtil.elementColor
                alignment = Alignment.LEFT
                textPadding = 25.0

                onHover {
                    if (stack != null && mouseY.toDouble() in visibleRange) {
                        StencilUtils.disable()
                        HUDRenderUtils.drawHoveringText(stack.getTooltip(), mouseX, mouseY)
                        StencilUtils.enable()
                    }
                }

                onClick {
                    val newEntry = if (item != null) "REPOITEM:${displayText.clean}" else displayText
                    debugMessage(newEntry)
                    val finalText = item?.let { processItem(it).noControlCodes } ?: displayText
                    debugMessage(finalText)
                    if (newEntry.isNotBlank()) history = history.addToHistory(newEntry)
                    sign?.let {
                        it.signText[0] = ChatComponentText(finalText)
                        it.markDirty()
                    } ?: commandAny("$commandPrefix ${finalText.noControlCodes}")
                    mc.displayGuiScreen(null)
                }

            }
            btn.render(mouseX, mouseY)
            stack?.let { drawItemStackWithText(it, x + 7.0, offsetY + 2.0) }
            resultButtons.add(btn)
            offsetY += 25.0
        }
    }

    protected open fun processItem(item: RepoItem): String = item.name

    protected abstract fun filterItems(query: String): List<RepoItem>

    private fun MutableList<String>.addToHistory(new: String): MutableList<String> = (listOf(new) + this).distinct().take(9).toMutableList()

    val String.clean: String
        get() = this.noControlCodes.replace("[Lvl {LVL}] ", "")

}

enum class SearchType {
    BAZAAR, AUCTION, NONE
}