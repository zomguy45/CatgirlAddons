package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.CatgirlRoutes.Companion.onHypixel

import catgirlroutes.events.PacketSentEvent
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.EtherWarpHelper.getEtherPos
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Zpew : Module(
    name = "Zpew",
    category = Category.MISC
) {
    private var lastYaw = 0f
    private var lastPitch = 0f
    val recentlySentC06s = mutableListOf<SentC06>()
    val recentFails = mutableListOf<Long>()

    override fun onKeyBind() {
        if (!this.enabled || onHypixel || mc.thePlayer == null) return
        val etherBlock = getEtherPos(
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
            mc.thePlayer.rotationYaw,
            mc.thePlayer.rotationPitch
        )
        if (!etherBlock.succeeded || etherBlock.pos == null) return

        val pos: BlockPos = etherBlock.pos;
        val x: Double = Math.floor(pos.x.toDouble()) + 0.5;
        val y: Double = pos.y + 1.05;
        val z: Double = Math.floor(pos.z.toDouble()) + 0.5;
        
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

        recentlySentC06s.add(SentC06(pitch, yaw, x, y, z, System.currentTimeMillis()))

        scheduleTask(0) {
            //mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, mc.thePlayer.onGround))
            //mc.thePlayer.setPosition(x, y, z)
        }

  }

    fun isWithinTolerance(n1: Float, n2: Float, tolerance: Double = 1e-4): Boolean {
        return kotlin.math.abs(n1 - n2) < tolerance
    }

    @SubscribeEvent
    fun onS08(event: ReceivePacketEvent) {
        if (event.packet !is S08PacketPlayerPosLook) return

        val sentC06 = recentlySentC06s.removeAt(0)

        val newPitch = event.packet.pitch
        val newYaw = event.packet.yaw
        val newX = event.packet.x
        val newY = event.packet.y
        val newZ = event.packet.z

        val isCorrect = isWithinTolerance(sentC06.pitch, newPitch) &&
                isWithinTolerance(sentC06.yaw, newYaw) &&
                sentC06.x == newX &&
                sentC06.y == newY &&
                sentC06.z == newZ

        if (isCorrect) {
            //event.isCanceled = true
            modMessage("Correct")
            return
        }

        recentFails.add(System.currentTimeMillis())

        recentlySentC06s.clear()
    }

    @SubscribeEvent
    fun onC03(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        lastYaw = event.packet.yaw
        lastPitch = event.packet.pitch
    }
}

data class SentC06(
    val pitch: Float,
    val yaw: Float,
    val x: Double,
    val y: Double,
    val z: Double,
    val sentAt: Long
)