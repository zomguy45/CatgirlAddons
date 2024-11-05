package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.PacketSentEvent
import catgirlroutes.mixins.accessors.IEntityPlayerSPAccessor
import catgirlroutes.utils.ChatUtils.modMessage
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoRouteUtils {
    private var cancelling = false
    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !cancelling) return
        if (!event.isCanceled) event.isCanceled = true
        modMessage(event.isCanceled.toString())
        cancelling = false
    }

    fun cancelRotate(yaw: Float, pitch: Float) {
        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val x: Double = mc.thePlayer.posX - player.lastReportedPosX
        val y: Double = mc.thePlayer.posY - player.lastReportedPosY
        val z: Double = mc.thePlayer.posZ - player.lastReportedPosZ
        val moving = x * x + y * y + z * z > 9.0E-40 || player.positionUpdateTicks >= 20
        modMessage(moving.toString())
        modMessage("$x | $y | $z || ${player.positionUpdateTicks}")
        if (moving) {
            modMessage("C06")
            mc.netHandler.networkManager.sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    yaw,
                    pitch,
                    mc.thePlayer.onGround
                )
            )
        } else {
            modMessage("C05")
            mc.netHandler.networkManager.sendPacket(C03PacketPlayer.C05PacketPlayerLook(
                yaw,
                pitch,
                mc.thePlayer.onGround
            ))
        }
        cancelling = true
    }
}