package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.PacketSentEvent
import catgirlroutes.mixins.accessors.IEntityPlayerSPAccessor
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.misc.ZpewRecode
import catgirlroutes.module.impl.misc.ZpewRecode.recentlySentC06s
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.ChatUtils.modMessage
import kotlinx.coroutines.processNextEventInCurrentThread
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.Sys

object AutoRouteUtils : Module(
    name = "CancelRotate",
    category = Category.MISC
) {

    private var cancelling = false
    /*var yawOne = 0f
    var pitchOne = 0f

    override fun onKeyBind() {
        cancelRotate(0f, 0f)
        devMessage("1")
    }*/
    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer || !cancelling) return
        /*if(!recentlySentC06s.isEmpty()) {
            val sentC06 = recentlySentC06s[0]
            if (sentC06.yaw !== yawOne || sentC06.pitch !== pitchOne) {
                devMessage("NIGGER")
                return
            }
        }*/
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
        if (cancelling) return
        val packet = if (moving) {
            modMessage("C06")
            C06PacketPlayerPosLook(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                yaw,
                pitch,
                mc.thePlayer.onGround
            )
        } else {
            modMessage("C05")
            C03PacketPlayer.C05PacketPlayerLook(
                yaw,
                pitch,
                mc.thePlayer.onGround
            )
        }
        PacketUtils.sendPacket(packet)
        /*yawOne = yaw
        pitchOne = pitch*/

        cancelling = true
    }
}