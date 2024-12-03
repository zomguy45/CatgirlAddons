package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.utils.PlayerUtils.airClick
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoRouteUtils {

    var rotating: Boolean = false
    var sending: Boolean = false
    var clicking: Boolean = false
    var skipNext: Boolean = false
    var skipNextNoS08: Boolean = false
    var y: Float = 0F
    var p: Float = 0F
    private var lastS08: PacketReceiveEvent? = null

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        if (event.packet is C06PacketPlayerPosLook && (skipNext || skipNextNoS08)) {
            skipNext = false
            if (lastS08 != null) {
                if (!lastS08!!.isCanceled) {
                    skipNextNoS08 = false
                    return
                }
            }
            if (skipNextNoS08) {
                skipNextNoS08 = false
                return
            }
        }
        if (!rotating || sending) return

        event.isCanceled = true

        sending = true

        if (event.packet is C05PacketPlayerLook) {
            mc.netHandler.addToSendQueue(C05PacketPlayerLook(y, p, event.packet.isOnGround))
        } else if (event.packet is C06PacketPlayerPosLook || event.packet is C04PacketPlayerPosition) {
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(event.packet.positionX, event.packet.positionY, event.packet.positionZ, y, p, event.packet.isOnGround))
        }

        sending = false

        if (clicking) {
            clicking = false
            airClick()
            y = mc.thePlayer.rotationYaw
            p = mc.thePlayer.rotationPitch
        } else rotating = false
    }

    @SubscribeEvent
    fun onPacketS08(event: PacketReceiveEvent) {
        if (event.packet !is S08PacketPlayerPosLook) return
        lastS08 = event
        skipNext = true
    }

    fun clickAt(yaw: Float, pitch: Float) {
        y = yaw
        p = pitch
        rotating = true
        clicking = true
    }
}