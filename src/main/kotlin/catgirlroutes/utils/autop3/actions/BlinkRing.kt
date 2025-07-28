package catgirlroutes.utils.autop3.actions

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.impl.dungeons.Blink
import catgirlroutes.module.impl.dungeons.Blink.packetArray
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.autop3.Ring
import catgirlroutes.utils.autop3.RingsManager.blinkCd
import catgirlroutes.utils.autop3.RingsManager.blinkEditMode
import catgirlroutes.utils.customtriggers.TypeName
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@TypeName("blink")
class BlinkRing(var packets: MutableList<Blink.BlinkC06> = mutableListOf()) : RingAction() {
    private var queuedBlink = false

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun execute(ring: Ring) {
        if (blinkEditMode) return
        if (packets.isEmpty()) return modMessage("Not enough packets.")
        if (packetArray >= packets.size && !blinkCd) {
            queuedBlink = true
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START && queuedBlink) {
            queuedBlink = false
            blinkCd = true
            packets.forEach { packet ->
                mc.netHandler.networkManager.sendPacket(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        packet.x,
                        packet.y,
                        packet.z,
                        packet.onGround
                    )
                )
                packetArray -= 1
            }
            mc.thePlayer.setPosition(packets.last().x, packets.last().y, packets.last().z)
            scheduleTask(1) {
                blinkCd = false
            }
        }
    }
}
