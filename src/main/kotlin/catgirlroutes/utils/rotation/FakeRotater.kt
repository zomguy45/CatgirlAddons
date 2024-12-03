package catgirlroutes.utils.rotation

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.MotionUpdateEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.mixins.accessors.IEntityPlayerSPAccessor
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.PlayerUtils.airClick
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


open class Rotater {
    companion object {
        var rotater: IRotater? = null
        var pitch: Float = 0f
        var yaw: Float = 0f
        var fakeRotating: Boolean = false
        var fakeClicking: Boolean = false
        var sneak: Boolean = false
        var checkingPacket = false

        @SubscribeEvent
        fun onMotionUpdatePre(event: MotionUpdateEvent.Pre) {
            if (fakeRotating) {
                event.yaw = yaw
                event.pitch = pitch
                modMessage("Yaw: $yaw Pitch: $pitch")
                fakeClicking = true
                fakeRotating = false
            }
        }
        @SubscribeEvent
        fun onMotionUpdatePost(event: MotionUpdateEvent.Post) {
            if (fakeClicking) {
                airClick()
                val player = mc.thePlayer as? IEntityPlayerSPAccessor
                modMessage("LYaw: ${player!!.lastReportedYaw} LPitch: ${player.lastReportedPitch}")
                fakeClicking = false
            }
        }

        @SubscribeEvent
        fun onPacket(event: PacketSentEvent) {
            if (event.packet !is C03PacketPlayer || !checkingPacket) return
            checkingPacket = false
            modMessage(event.packet.javaClass)
            modMessage("PYaw: ${event.packet.yaw} PPitch: ${event.packet.pitch}")
        }
    }
}

object FakeRotater : Rotater(), IRotater {
    override fun rotate(yaw: Float, pitch: Float) {
        Companion.sneak = sneak
        Companion.yaw = yaw
        Companion.pitch = pitch
        fakeRotating = true
        checkingPacket = true
        modMessage("started")
    }

    fun stopRotating() {
        fakeRotating = false
        modMessage("stopped")
    }
}

interface IRotater {
    fun rotate(yaw: Float, pitch: Float,)
}