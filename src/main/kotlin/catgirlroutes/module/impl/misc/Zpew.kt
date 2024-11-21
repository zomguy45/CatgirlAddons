package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.PacketSentEvent
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.LocationManager.inSkyblock
import catgirlroutes.utils.rotation.ServerRotateUtils
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.skyblockID
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ZpewRecode : Module(
    name = "ZpewRecode",
    category = Category.DUNGEON
) {
    var updatePosition = true
    private val recentlySentC06s = mutableListOf<SentC06>()
    val recentFails = mutableListOf<Long>()

    var lastPitch: Float = 0f
    var lastYaw: Float = 0f
    var lastX: Double = 0.0
    var lastY: Double = 0.0
    var lastZ: Double = 0.0
    var isSneaking: Boolean = false
    var toggled = false

    override fun onKeyBind() {
        toggled = !toggled
        modMessage(toggled)
        if (toggled) ServerRotateUtils.set(0f, 0f)
        else ServerRotateUtils.resetRotations()
        devMessage(lastYaw)
        devMessage(lastPitch)
    }

    fun doZeroPingEtherWarp() {
        val etherBlock = EtherWarpHelper.getEtherPos(
            Vec3(lastX, lastY, lastZ),
            lastYaw,
            lastPitch,
            57.0
        )
        if (!etherBlock.succeeded || etherBlock.pos == null) return
        val pos: BlockPos = etherBlock.pos!!;
        val x: Double = pos.x.toDouble() + 0.5;
        val y: Double = pos.y.toDouble() + 1.05;
        val z: Double = pos.z.toDouble() + 0.5;
        var yaw = lastYaw
        val pitch = lastPitch
        yaw %= 360
        if (yaw < 0) yaw += 360
        if (yaw > 360) yaw -= 360
        lastX = x
        lastY = y
        lastZ = z
        updatePosition = false

        recentlySentC06s.add(SentC06(pitch, yaw, x, y, z, System.currentTimeMillis()))

        scheduleTask(0) {
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, mc.thePlayer.onGround))
            mc.thePlayer.setVelocity(0.0, 0.0, 0.0)
            mc.thePlayer.setPosition(x, y, z)
            updatePosition = true
        }
    }

    fun isWithinTolerance(n1: Float, n2: Float, tolerance: Double = 1e-4): Boolean {
        return kotlin.math.abs(n1 - n2) < tolerance
    }

    @SubscribeEvent
    fun onC08(event: PacketSentEvent) {
        if (mc.thePlayer == null || !this.enabled) return
        if (event.packet !is C08PacketPlayerBlockPlacement) return

        val dir = event.packet.placedBlockDirection
        if (dir != 255) return

        val held =  mc.thePlayer.heldItem.skyblockID
        if (held != "ASPECT_OF_THE_VOID") return
        if (!isSneaking) return

        doZeroPingEtherWarp()
    }

    @SubscribeEvent
    fun onC03(event: PacketSentEvent) {
        if (event.packet !is C03PacketPlayer) return
        if (!updatePosition) return
        val x = event.packet.positionX
        val y = event.packet.positionY
        val z = event.packet.positionZ
        val yaw = event.packet.yaw
        val pitch = event.packet.pitch

        if (event.packet.isMoving) {
            lastX = x
            lastY = y
            lastZ = z
        }

        if (event.packet.rotating) {
            lastYaw = yaw
            lastPitch = pitch
        }
    }

    @SubscribeEvent
    fun onC0B(event: PacketSentEvent) {
        if (event.packet !is C0BPacketEntityAction || !inSkyblock) return
        if (event.packet.action == C0BPacketEntityAction.Action.START_SNEAKING) isSneaking = true;
        if (event.packet.action == C0BPacketEntityAction.Action.STOP_SNEAKING) isSneaking = false;
    }

    @SubscribeEvent
    fun onS08(event: ReceivePacketEvent) {
        if (event.packet !is S08PacketPlayerPosLook) return
        if (recentlySentC06s.isEmpty()) return

        val sentC06 = recentlySentC06s.removeFirst()

        val newPitch = event.packet.pitch
        val newYaw = event.packet.yaw
        val newX = event.packet.x
        val newY = event.packet.y
        val newZ = event.packet.z

        devMessage(newPitch)
        devMessage(newYaw)
        devMessage(newX)
        devMessage(newY)
        devMessage(newZ)

        devMessage(sentC06)

        val isCorrect = (
                (isWithinTolerance(newPitch, sentC06.pitch) || newPitch == 0f) &&
                (isWithinTolerance(newYaw, sentC06.yaw) || newYaw == 0f) &&
                newX == sentC06.x &&
                newY == sentC06.y &&
                newZ == sentC06.z
                )

        if (isCorrect) {
            modMessage("Correct")
            event.isCanceled = true
        } else {
            modMessage("Failed")
            recentlySentC06s.clear()
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
}

