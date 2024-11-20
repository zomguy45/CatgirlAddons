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
import me.odinmain.utils.skyblock.EtherWarpHelper
import me.odinmain.utils.skyblock.skyblockID
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ZpewRecode : Module(
    name = "ZpewRecode",
    category = Category.DUNGEON
) {
    var updatePosition = true
    val recentlySentC06s = mutableListOf<SentC06>()
    val recentFails = mutableListOf<Long>()

    data class playerInfo(
        var pitch: Float = 0f,
        var yaw: Float = 0f,
        var x: Double = 0.0,
        var y: Double = 0.0,
        var z: Double = 0.0,
        var sneaking: Boolean = false
        )

    data class SentC06(
        val pitch: Float,
        val yaw: Float,
        val x: Double,
        val y: Double,
        val z: Double,
        val sentAt: Long
    )

    fun etherBlock(): Vec3? {
        val playerInfo = playerInfo()
        val ether = EtherWarpHelper.getEtherPos(
            Vec3(playerInfo.x, playerInfo.y, playerInfo.z),
            playerInfo.yaw,
            playerInfo.pitch,
            60.0
        )
        return if (ether.succeeded && ether.pos != null) {
            Vec3(ether.pos!!.x.toDouble(), ether.pos!!.y.toDouble(), ether.pos!!.z.toDouble())
        } else {
            null
        }
    }

    fun doZeroPingEtherWarp() {
        devMessage("1")
        var x = etherBlock()!!.xCoord
        var y = etherBlock()!!.yCoord
        var z = etherBlock()!!.zCoord
        devMessage("2")
        x += 0.5
        y += 1.05
        z += 0.5
        devMessage("3")
        val yaw = playerInfo().yaw
        val pitch = playerInfo().pitch
        devMessage("4")
        playerInfo().x = x
        playerInfo().y = y
        playerInfo().z = z
        devMessage("5")
        updatePosition = false

        recentlySentC06s.add(SentC06(pitch, yaw, x, y, z, System.currentTimeMillis()))

        scheduleTask(0) {
            devMessage("6")
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
        if (mc.thePlayer == null || !this.enabled) return
        if (event.packet !is C08PacketPlayerBlockPlacement) return
        modMessage("7")
        val dir = event.packet.placedBlockDirection
        if (dir != 255) return
        modMessage("8")
        val held =  mc.thePlayer.heldItem.skyblockID
        if (held != "ASPECT_OF_THE_VOID") return
        modMessage("9")
        if (!playerInfo().sneaking) return
        modMessage("10")
        modMessage("DEBUG")

        doZeroPingEtherWarp()
    }

    @SubscribeEvent
    fun onC03(event: ReceivePacketEvent) {
        if (event.packet !is C03PacketPlayer) return

        if (!updatePosition) return
        val x = event.packet.positionX
        val y = event.packet.positionY
        val z = event.packet.positionZ
        val yaw = event.packet.yaw
        val pitch = event.packet.pitch

        if (event.packet.isMoving) {
            playerInfo().x = x
            playerInfo().y = y
            playerInfo().z = z
        }

        if (event.packet.rotating) {
            playerInfo().yaw = yaw
            playerInfo().pitch = pitch
        }
    }

    @SubscribeEvent
    fun onC0B(event: PacketSentEvent) {
        if (event.packet !is C0BPacketEntityAction || !inSkyblock) return
        devMessage("C0B")
        if (event.packet.action == C0BPacketEntityAction.Action.START_SNEAKING) playerInfo().sneaking = true;
        if (event.packet.action == C0BPacketEntityAction.Action.STOP_SNEAKING) playerInfo().sneaking = false;
    }

    @SubscribeEvent
    fun onS08(event: ReceivePacketEvent) {
        if (event.packet !is S08PacketPlayerPosLook) return
        modMessage("NIGGER")
        if (recentlySentC06s.isEmpty()) return

        val sentC06 = Zpew.recentlySentC06s[0]
        Zpew.recentlySentC06s.removeFirst()

        val newPitch = event.packet.pitch
        val newYaw = event.packet.yaw
        val newX = event.packet.x
        val newY = event.packet.y
        val newZ = event.packet.z

        val isCorrect = (
                (isWithinTolerance(newPitch, sentC06.pitch)|| newPitch == 0f) &&
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
        }
    }
}

