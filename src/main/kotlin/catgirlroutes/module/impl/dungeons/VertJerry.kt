package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.LocationManager
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object VertJerry : Module(
    "Vert Jerry",
    category = Category.DUNGEON,
    description = "Cancels horizontal kb from jerrychine gun"
){
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (!LocationManager.inSkyblock) return
        if (event.packet !is S12PacketEntityVelocity || !this.enabled) return
        if (event.packet.entityID != mc.thePlayer.entityId) return
        if (event.packet.motionY == 4800) {
            event.isCanceled = true
            mc.thePlayer.setVelocity(mc.thePlayer.motionX, 0.6, mc.thePlayer.motionZ)
        }
    }
}