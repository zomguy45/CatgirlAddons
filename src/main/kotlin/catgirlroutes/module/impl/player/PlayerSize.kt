package catgirlroutes.module.impl.player

import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.CgaUsers
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager


object PlayerSize : Module(
    "Player Size",
    Category.PLAYER,
){
    val scaleX = NumberSetting("Scale X", 1.0, -3.0, 3.0, 0.1)
    val scaleY = NumberSetting("Scale Y", 1.0, -3.0, 3.0, 0.1)
    val scaleZ = NumberSetting("Scale Z", 1.0, -3.0, 3.0, 0.1)

    init {
        addSettings(this.scaleX, this.scaleY, this.scaleZ, ClickGui.updateUser)
    }

    fun scaleHook(entityLivingBaseIn: AbstractClientPlayer) {
        if (!CgaUsers.users.containsKey(entityLivingBaseIn.name)) return

        val user = CgaUsers.users[entityLivingBaseIn.name] ?: return
        GlStateManager.scale(user.xScale, user.yScale, user.zScale)
        if (user.yScale < 0) GlStateManager.translate(0f, user.yScale * -2, 0f)
    }
}