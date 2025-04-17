package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.clickgui.util.FontUtil
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
        FontUtil.drawAlignedString(value, x, y, this.style.alignment)
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

    infix fun onHover(action: () -> Unit): ButtonBuilder {
        onHoverAction = action
        return this
    }

    infix fun onClick(action: () -> Unit): MiscElementButton {
        onClickAction = action
        return build()
    }

    override fun buildElement(): MiscElementButton {
        return MiscElementButton(createStyle(), onClickAction, onHoverAction)
    }
}

fun button(block: ButtonBuilder.() -> Unit): ButtonBuilder {
    return ButtonBuilder().apply(block)
}