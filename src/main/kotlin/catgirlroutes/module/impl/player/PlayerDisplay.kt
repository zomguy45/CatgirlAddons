package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.ui.hud.HudElement
import java.awt.Color

object PlayerDisplay: Module(
    name = "Player Display",
    category = Category.PLAYER
) {
    private val speed = BooleanSetting("Speed", false)

    init {
        this.addSettings(speed)
    }

    @RegisterHudElement
    object Speed : HudElement(
        this,
        0, 0,
        mc.fontRendererObj.getStringWidth("500✦") + 6,
        (mc.fontRendererObj.FONT_HEIGHT + 2)
    ) {
        override fun renderHud() {
            if (mc.thePlayer == null || !speed.value) return
            val speed = (mc.thePlayer.capabilities.walkSpeed * 1000).toInt()
            mc.fontRendererObj.drawStringWithShadow("${speed}§f✦", 6.0F, 0.0f, Color.PINK.rgb)
        }
    }
}