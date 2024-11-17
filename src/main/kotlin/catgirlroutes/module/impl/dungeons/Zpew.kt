package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.CatgirlRoutes.Companion.onHypixel

import catgirlroutes.events.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.EtherWarpHelper.getEtherPos
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object Zpew : Module(
    name = "Zpew",
    category = Category.MISC
) {
    var lastYaw = 0f
    var lastPitch = 0f

    override fun onKeyBind() {
        if (!this.enabled || onHypixel || mc.thePlayer == null) return
        val etherBlock = getEtherPos(Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        if (!etherBlock.succeeded) return
        val x = etherBlock.pos?.x!!.toDouble()
        val y = etherBlock.pos.y.toDouble()
        val z = etherBlock.pos.z.toDouble()
        
        mc.thePlayer.setPosition(floor(x) + 0.5,y + 1.05, floor(z) + 0.5)
    }

    fun doZeroPingEtherWarp(yaw: Float, pitch: Float) {
        val etherBlock = getEtherPos(Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        if (!etherBlock.succeeded) return
        var x = etherBlock.pos?.x!!.toDouble()
        var y = etherBlock.pos.y.toDouble()
        var z = etherBlock.pos.z.toDouble()

        x += 0.5
        y += 1.05
        z += 0.5

        scheduleTask(0) {
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, mc.thePlayer.onGround))
            mc.thePlayer.setPosition(x, y, z)
        }
  }

    @SubscribeEvent
    fun onC03(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        lastYaw = event.packet.yaw
        lastPitch = event.packet.pitch
    }
}