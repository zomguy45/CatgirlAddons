package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.NumberSetting
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager


object PlayerSize : Module(
    "Player Size",
    Category.PLAYER,
){
    private val scaleX = NumberSetting("Scale X", 1.0, -3.0, 3.0, 0.1)
    private val scaleY = NumberSetting("Scale Y", 1.0, -3.0, 3.0, 0.1)
    private val scaleZ = NumberSetting("Scale Z", 1.0, -3.0, 3.0, 0.1)

    init {
        addSettings(this.scaleX, this.scaleY, this.scaleZ)
    }

    fun scaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!this.enabled) return
        if (!mc.thePlayer.name.equals(entityLivingBaseIn.name)) return

        GlStateManager.scale(scaleX.value, scaleY.value, scaleZ.value)
        if (scaleY.value < 0) GlStateManager.translate(0.0, scaleY.value * -2, 0.0)
    }
}