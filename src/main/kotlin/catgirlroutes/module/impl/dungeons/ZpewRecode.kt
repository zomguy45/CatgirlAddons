package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.PacketSentEvent
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.chatMessage
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ChatUtils.sendChat
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.LocationManager.inSkyblock
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
    private const val FAILWATCHPERIOD: Int = 20;
    private const val MAXFAILSPERFAILPERIOD: Int = 3;
    private const val MAXQUEUEDPACKETS: Int = 3;

    private var updatePosition = true
    private val recentlySentC06s = mutableListOf<SentC06>()
    private val recentFails = mutableListOf<Long>()

    private var lastPitch: Float = 0f
    private var lastYaw: Float = 0f
    private var lastX: Double = 0.0
    private var lastY: Double = 0.0
    private var lastZ: Double = 0.0
    private var isSneaking: Boolean = false

    private fun checkAllowedFails() : Boolean {
        if (recentlySentC06s.size >= MAXQUEUEDPACKETS) return false;

        while (recentFails.size != 0 && System.currentTimeMillis() - recentFails[0] > FAILWATCHPERIOD * 1000) recentFails.removeFirst();

        return recentFails.size < MAXFAILSPERFAILPERIOD;
    }

    private fun doZeroPingEtherWarp() {
        val etherBlock = EtherWarpHelper.getEtherPos(
            Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
            mc.thePlayer.rotationYaw,
            mc.thePlayer.rotationPitch,
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

        recentlySentC06s.add(SentC06(yaw, pitch, x, y, z, System.currentTimeMillis()))

        scheduleTask(0) {
            mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, yaw, pitch, mc.thePlayer.onGround))
            mc.thePlayer.setPosition(x, y, z)
            mc.thePlayer.setVelocity(0.0, 0.0, 0.0)
            updatePosition = true
        }
    }

    fun isWithinTolerance(n1: Float, n2: Float, tolerance: Double = 1e-4): Boolean {
        return kotlin.math.abs(n1 - n2) < tolerance
    }

    @SubscribeEvent
    fun onC08(event: PacketSentEvent) {
        if (mc.thePlayer == null) return
        if (event.packet !is C08PacketPlayerBlockPlacement) return

        val dir = event.packet.placedBlockDirection
        if (dir != 255) return

        val held =  mc.thePlayer.heldItem.skyblockID
        if (held != "ASPECT_OF_THE_VOID") return
        if (!isSneaking) return

        if(!checkAllowedFails()) {
            chatMessage("§cZero ping etherwarp teleport aborted.");
            chatMessage("§c${recentFails.size} fails last ${FAILWATCHPERIOD}s");
            chatMessage("§c${recentlySentC06s.size} C06's queued currently");
            return
        }

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

        val sentC06 = recentlySentC06s[0]
        recentlySentC06s.removeFirst()

        val newYaw = event.packet.yaw
        val newPitch = event.packet.pitch
        val newX = event.packet.x
        val newY = event.packet.y
        val newZ = event.packet.z

        devMessage("newYaw: $newYaw")
        devMessage("newPitch: $newPitch")
        devMessage("newX: $newX")
        devMessage("newY: $newY")
        devMessage("newZ: $newZ")

        devMessage(sentC06)

        val isCorrect = (
                (isWithinTolerance(newYaw, sentC06.yaw) || newYaw == 0f) &&
                (isWithinTolerance(newPitch, sentC06.pitch) || newPitch == 0f) &&
                newX == sentC06.x &&
                newY == sentC06.y &&
                newZ == sentC06.z
                )

        if (isCorrect) {
            devMessage("Correct")
            event.isCanceled = true
            return
        }
        devMessage("Failed");
        recentFails.add(System.currentTimeMillis());
        while (recentlySentC06s.size > 0) recentlySentC06s.removeFirst();
    }

    data class SentC06(
        val yaw: Float,
        val pitch: Float,
        val x: Double,
        val y: Double,
        val z: Double,
        val sentAt: Long
    )
}

