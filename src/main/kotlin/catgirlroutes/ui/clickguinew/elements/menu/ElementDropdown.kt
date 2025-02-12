package catgirlroutes.ui.clickguinew.elements.menu

import catgirlroutes.CatgirlRoutes.Companion.RESOURCE_DOMAIN
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.ui.animations.impl.EaseOutQuadAnimation
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickguinew.elements.Element
import catgirlroutes.ui.clickguinew.elements.ElementType
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedBorderedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawTexturedRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color

class ElementDropdown(parent: ModuleButton, setting: DropdownSetting) : // todo anim, icon
    Element<DropdownSetting>(parent, setting, ElementType.DROPDOWN) {

    private var extraHeight = 0.0
    private var arrowAnimation = EaseOutQuadAnimation(300)

    override fun renderElement(mouseX: Int, mouseY: Int, partialTicks: Float): Double {
        drawRoundedBorderedRect(-3.0, 0.0, width + 6.0, height + this.extraHeight, 3.0, 1.0, Color(ColorUtil.outlineColor).darker(), ColorUtil.clickGUIColor)
        FontUtil.drawString(displayName, 0.0, 3.0)

        val rotation = this.arrowAnimation.get(0.0, -90.0, !this.setting.enabled)
        GlStateManager.translate(width - 15.0 + 6.5, 0.0 + 6.5, 0.0)
        GlStateManager.rotate(rotation.toFloat(), 0.0f, 0.0f, 1.0f)
        GlStateManager.translate(-(width - 15.0 + 6.5), -(0.0 + 6.5), 0.0)
        mc.textureManager.bindTexture(arrowIcon)
        drawTexturedRect(width - 15.0, 0.0, 13.0, 13.0)
        return super.renderElement(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY) && this.arrowAnimation.start()) {
            this.setting.enabled = !this.setting.enabled
            this.parent.updateElements()
            this.extraHeight = if (this.setting.enabled) this.parent.elementsHeight - this.parent.prevHeight
            else 0.0
            return true
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= xAbsolute && mouseX <= xAbsolute + width && mouseY >= yAbsolute && mouseY <= yAbsolute + height
    }

    companion object {
        val arrowIcon = ResourceLocation(RESOURCE_DOMAIN, "arrow.png")
    }
}