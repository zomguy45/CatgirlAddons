package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.ellipsize
import catgirlroutes.ui.misc.elements.ElementDSL
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.ui.misc.elements.MiscElementStyle
import catgirlroutes.ui.misc.elements.util.calculateTextPosition
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.renderer.GlStateManager

class MiscElementButton(
    style: MiscElementStyle = MiscElementStyle(),
    private val onClick: () -> Unit,
    private val onHover: () -> Unit
) : MiscElement(style) {

    override fun render(mouseX: Int, mouseY: Int) {
        GlStateManager.pushMatrix()
        HUDRenderUtils.drawRoundedBorderedRect(
            x, y, width, height, radius, thickness,
            colour, if (isHovered(mouseX, mouseY)) outlineHoverColour else outlineColour
        )
        val (x, y) = this.calculateTextPosition()
        FontUtil.drawAlignedString(value.ellipsize(width - 3.0), x, y, this.style.alignment)
        if (isHovered(mouseX, mouseY)) this.onHover.invoke()
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            this.onClick.invoke()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}

class ButtonBuilder : ElementDSL<MiscElementButton>() {
    var text by _style::value
    private var onClickAction: () -> Unit = {}
    private var onHoverAction: () -> Unit = {}

    fun onHover(action: () -> Unit): ButtonBuilder { // todo make it in main class
        onHoverAction = action
        return this
    }

    fun onClick(action: () -> Unit): ButtonBuilder {
        onClickAction = action
        return this
    }

    override fun buildElement(): MiscElementButton {
        return MiscElementButton(createStyle(), onClickAction, onHoverAction)
    }
}

fun button(block: ButtonBuilder.() -> Unit): MiscElementButton {
    return ButtonBuilder().apply(block).build()
}