package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.PacketSentEvent
import catgirlroutes.events.ReceivePacketEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils.modMessage
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0CPacketInput
import net.minecraft.network.play.server.S1BPacketEntityAttach
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs
import kotlin.math.pow

object InstaMid : Module(
    "Insta mid",
    category = Category.DUNGEON,
    description = "A module that instantly teleports you to necrons platform."
){
    private var preparing = false
    private var active = false

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (!active) return
        if (event.packet !is C03PacketPlayer && event.packet !is C0CPacketInput) return
        event.isCanceled = true
        val riding = mc.thePlayer.isRiding
        if (riding) preparing = false
        if (!riding && !preparing) {
            active = false
            preparing = true
            mc.netHandler.networkManager.sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    54.0,
                    65.0,
                    76.0,
                    0F,
                    0F,
                    false
                )
            )
        }
    }

    @SubscribeEvent
    fun onPacketReceive(event: ReceivePacketEvent) {
        if (!this.enabled) return
        if (event.packet !is S1BPacketEntityAttach) return
        if (!isOnPlatform()) return
        if (event.packet.entityId != mc.thePlayer.entityId) return
        if (event.packet.vehicleEntityId < 0) return
        preparing = true
        active = true
        modMessage("Attempting to instamid")
    }

    private fun isOnPlatform(): Boolean {
        if (mc.thePlayer.posY > 100) return false
        if (mc.thePlayer.posY < 64) return false
        return abs(mc.thePlayer.posX - 54.5).pow(2) + abs(mc.thePlayer.posZ - 76.5).pow(2) < 56.25
    }
}