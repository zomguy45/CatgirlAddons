package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.PacketSentEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerRotateUtils {
    private var sending: Boolean = false
    private var yaw: Float? = null
    private var pitch: Float? = null
    private var resetRot: Boolean = false
    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        if (sending) return
        if (yaw == null || pitch == null || mc.thePlayer.isRiding) return
        if (!event.isCanceled) event.isCanceled = true
        sending = true
        val wasOnGround = event.packet.isOnGround
        if (event.packet is C03PacketPlayer.C05PacketPlayerLook) {
            mc.netHandler.networkManager.sendPacket(C03PacketPlayer.C05PacketPlayerLook(
                yaw!!,
                pitch!!,
                wasOnGround
            ))
        }
        else {
            mc.netHandler.networkManager.sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                yaw!!,
                pitch!!,
                wasOnGround
            ))
        }
        if (resetRot) {
            resetRot = false
            yaw = null
            pitch = null
        }
        sending = false
    }
    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        reset()
    }
    fun set(y: Float, p: Float) {
        yaw = y
        pitch = p
    }
    private fun reset() {
        yaw = null
        pitch = null
    }
    fun resetRotations() {
        set(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        resetRot = true
    }
}
