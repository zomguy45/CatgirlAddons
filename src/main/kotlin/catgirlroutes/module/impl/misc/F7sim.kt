package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.CatgirlRoutes.Companion.onHypixel
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.utils.Island
import catgirlroutes.utils.LocationManager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object F7sim : Module(
    "F7 sim",
    category = Category.MISC,
    description = "Simulates 500 speed and lava bounce on single player."
){
    val highbounce = BooleanSetting("High Bounce", false)

    init {
        this.addSettings(highbounce)
    }
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!LocationManager.currentArea.isArea(Island.SinglePlayer)) return

        mc.thePlayer.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).baseValue = 0.50000000745
        mc.thePlayer.capabilities?.setPlayerWalkSpeed(0.5f)
        var h = 3.5
        if (highbounce.value) {
            h = 7.0
        }
        if (mc.thePlayer.isInLava) {
            mc.thePlayer.setVelocity(mc.thePlayer.motionX, h, mc.thePlayer.motionZ)
        }
    }
}