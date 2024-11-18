package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.CatgirlRoutes.Companion.onHypixel

import catgirlroutes.events.PacketSentEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.EtherWarpHelper.getEtherPos
import jdk.nashorn.internal.ir.Block
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object Zpew : Module(
    name = "Zpew",
    category = Category.MISC
) {
    private var lastYaw = 0f
    private var lastPitch = 0f

    override fun onKeyBind() {
        if (!this.enabled || onHypixel || mc.thePlayer == null) return
        val etherBlock = getEtherPos(
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
            mc.thePlayer.rotationYaw,
            mc.thePlayer.rotationPitch
        )
        if (!etherBlock.succeeded || etherBlock.pos == null) return

        val pos: BlockPos = etherBlock.pos;
        val x: Double = floor(pos.x.toDouble()) + 0.5;
        val y: Double = pos.y + 1.05;
        val z: Double = floor(pos.z.toDouble()) + 0.5;
        
        mc.thePlayer.setPosition(x, y, z)
    }

    fun doZeroPingEtherWarp(yaw: Float, pitch: Float) {
        val etherBlock = getEtherPos(
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
            mc.thePlayer.rotationYaw,
            mc.thePlayer.rotationPitch
        )
        if (!etherBlock.succeeded || etherBlock.pos == null) return

        val pos: BlockPos = etherBlock.pos;
        val x: Double = pos.x.toDouble() + 0.5;
        val y: Double = pos.y.toDouble() + 1.05;
        val z: Double = pos.z.toDouble() + 0.5;

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