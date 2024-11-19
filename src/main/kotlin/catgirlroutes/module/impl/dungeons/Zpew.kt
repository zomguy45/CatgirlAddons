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
import me.odinmain.utils.skyblock.skyblockID
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
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
    private var isSneaking = false
    val recentlySentC06s = mutableListOf<SentC06>()
    val recentFails = mutableListOf<Long>()

    override fun onKeyBind() {
        doZeroPingEtherWarp(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
    }

    fun doZeroPingEtherWarp(yaw: Float, pitch: Float) {
        val etherBlock = getEtherPos(
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
            mc.thePlayer.rotationYaw,
            mc.thePlayer.rotationPitch,
            57.0
        )
        if (!etherBlock.succeeded || etherBlock.pos == null) return

        val pos: BlockPos = etherBlock.pos;
        val x: Double = pos.x.toDouble() + 0.5;
        val y: Double = pos.y.toDouble() + 1.05;
        val z: Double = pos.z.toDouble() + 0.5;

        recentlySentC06s.add(SentC06(pitch, yaw, x, y, z, System.currentTimeMillis()))

        scheduleTask(0) {
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, mc.thePlayer.onGround))
            mc.thePlayer.setPosition(x, y, z)
            mc.thePlayer.setVelocity(0.0, 0.0, 0.0)
        }
  }

    fun isWithinTolerance(n1: Float, n2: Float, tolerance: Double = 1e-4): Boolean {
        return kotlin.math.abs(n1 - n2) < tolerance
    }

    @SubscribeEvent
    fun onS08(event: ReceivePacketEvent) {
        if (mc.thePlayer == null ||!this.enabled || mc.theWorld == null) return
        if (event.packet !is S08PacketPlayerPosLook) return

        if (recentlySentC06s.isEmpty()) return
        val sentC06 = recentlySentC06s.removeFirst()

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
            event.isCanceled = true
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

    @SubscribeEvent
    fun onC08(event: PacketSentEvent) {
        if (mc.thePlayer == null) return
        if (event.packet !is C08PacketPlayerBlockPlacement) return
        if (!this.enabled) return
        val dir = event.packet.placedBlockDirection
        if (dir !== 255) return

        val held =  mc.thePlayer.heldItem.skyblockID
        if (held !== "ASPECT_OF_THE_VOID") return
        if (!isSneaking) return

        doZeroPingEtherWarp(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
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