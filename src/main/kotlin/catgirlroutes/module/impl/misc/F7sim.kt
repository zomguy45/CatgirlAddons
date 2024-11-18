package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.CatgirlRoutes.Companion.onHypixel
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object F7sim : Module(
    "F7 sim",
    category = Category.MISC,
    description = "Simulates 500 speed and lava bounce on single player."
){
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (onHypixel || mc.thePlayer == null) return

        mc.thePlayer.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).baseValue = 0.50000000745
        
        if (mc.thePlayer.isInLava) {
            mc.thePlayer.setVelocity(mc.thePlayer.motionX, 3.37, mc.thePlayer.motionZ)
        }
    }
}