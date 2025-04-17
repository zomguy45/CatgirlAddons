package catgirlroutes.ui.misc.elements.impl

import catgirlroutes.ui.animations.impl.ColorAnimation
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.ElementDSL
import catgirlroutes.ui.misc.elements.MiscElement
import catgirlroutes.ui.misc.elements.MiscElementStyle
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedOutline

class MiscElementBoolean(
    style: MiscElementStyle = MiscElementStyle(),
    var enabled: Boolean = false,
    var gap: Double = 1.0
) : MiscElement(style) {
    var onChange: ((Boolean) -> Unit)? = null

    private val colourAnimation = ColorAnimation(250)

    override fun render(mouseX: Int, mouseY: Int) {
        val colour = colourAnimation.get(outlineHoverColour, colour, this.enabled)
        drawRoundedBorderedRect(x + this.gap, y + this.gap, width - this.gap * 2, height - this.gap * 2, radius, thickness, colour, colour)
        drawRoundedOutline(
            x, y, width, height, radius, thickness,
            if (isHovered(mouseX, mouseY)) outlineHoverColour else outlineColour
        )

        FontUtil.drawString(this.value, x + width + 5.0, y + height / 2 - FontUtil.fontHeight / 2)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (isHovered(mouseX, mouseY) && mouseButton == 0 && colourAnimation.start()) {
            this.enabled = !this.enabled
            this.onChange?.invoke(this.enabled)
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }
}

class BooleanBuilder : ElementDSL<MiscElementBoolean>() {
    var text by _style::value
    var gap: Double = 1.0
    var enabled: Boolean = false
    private var onChangeAction: (Boolean) -> Unit = {}

    infix fun onChange(action: (Boolean) -> Unit): MiscElementBoolean {
        onChangeAction = action
        return build()
    }

    override fun buildElement(): MiscElementBoolean {
        return MiscElementBoolean(createStyle(), enabled, gap).apply {
            onChange = { newValue ->
                onChangeAction(newValue)
            }
        }
    }
}

fun boolean(block: BooleanBuilder.() -> Unit): BooleanBuilder {
    return BooleanBuilder().apply(block)
}