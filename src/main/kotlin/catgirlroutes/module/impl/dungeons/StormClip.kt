package catgirlroutes.module.impl.dungeons;

import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Visibility
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.Utils.relativeClip
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object StormClip : Module(
    "Storm Clip",
    category = Category.MISC,
    description = "Clips you down into p2 at start of boss."
){
    private val stormclipdistance: NumberSetting = NumberSetting("Storm Clip distance", 30.0, 20.0, 50.0, 1.0, visibility = Visibility.ADVANCED_ONLY, description = "Distance to clip down")
    private var clipped = false
    init {
        this.addSettings(
            stormclipdistance
        )
    }
    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        clipped = false
    }
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S08PacketPlayerPosLook || !this.enabled || clipped) return
        if (event.packet.x == 73.5 && event.packet.y == 221.5 && event.packet.z == 14.5) {
            clipped = true
            scheduleTask(1) {relativeClip(0.0, stormclipdistance.value * -1, 0.0)}
        }
    }
}
