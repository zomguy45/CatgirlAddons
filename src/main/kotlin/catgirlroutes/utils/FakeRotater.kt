package catgirlroutes.utils

import catgirlroutes.events.MotionUpdateEvent
import catgirlroutes.events.MotionUpdateEvent.PreMotionUpdateEvent
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.Utils.airClick
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


open class Rotater {
    companion object {
        var rotater: IRotater? = null
        var pitch: Float = 0f
        var yaw: Float = 0f
        var fakeRotating: Boolean = false
        var fakeClicking: Boolean = false

        @SubscribeEvent
        fun onMotionUpdatePre(event: PreMotionUpdateEvent) {
            if (fakeRotating) {
                event.yaw = yaw
                event.pitch = pitch
                modMessage("Yaw: $yaw Pitch: $pitch")
                fakeClicking = true
                fakeRotating = false
            }
        }
        @SubscribeEvent
        fun onMotionUpdatePost(event: MotionUpdateEvent.PostMotionUpdateEvent) {
            if (fakeClicking) {
                airClick()
                fakeClicking = false
            }
        }
    }
}

object FakeRotater : Rotater(), IRotater {
    override fun rotate(yaw: Float, pitch: Float) {
        Rotater.yaw = yaw
        Rotater.pitch = pitch
        fakeRotating = true
        modMessage("started")
    }

    fun stopRotating() {
        fakeRotating = false
        modMessage("stopped")
    }
}

interface IRotater {
    fun rotate(yaw: Float, pitch: Float)
}