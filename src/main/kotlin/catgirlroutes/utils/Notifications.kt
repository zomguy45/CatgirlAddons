package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.notification.Notification
import catgirlroutes.ui.notification.NotificationType
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.pow

/**
 * Usage: Notifications.send()
 */
object Notifications {
    private val notifications = mutableListOf<Notification>()

    fun send(title: String, description: String, duration: Double = 2000.0, type: NotificationType = NotificationType.INFO) {
        notifications.add(Notification(title, description, duration, type))
    }

    @SubscribeEvent
    fun onRender(event: TickEvent.RenderTickEvent) {
        if (event.phase != TickEvent.Phase.END || notifications.isEmpty()) return

        notifications.removeIf { it.endTime <= System.currentTimeMillis() }

        val sr = ScaledResolution(mc)
        var y: Double = sr.scaledHeight - 3.0

        for (notification in notifications) {
            val width: Double = maxOf(150.0, mc.fontRendererObj.getStringWidth(notification.title) + 10.0)

            val lines = notification.wrapText(notification.description, width - 12)
            var height: Double = 20.0 + lines.size * (mc.fontRendererObj.FONT_HEIGHT + 2)

            var x: Double = sr.scaledWidth - width - 2

            val elapsedTime: Long = System.currentTimeMillis() - notification.startTime
            val remainingTime: Long = notification.endTime - System.currentTimeMillis()

            x += calculateX(elapsedTime, width) + calculateX(remainingTime, width)

            notification.draw(x, y - height, width, height)

            height *= calculateH(elapsedTime) // probably should change y instead of height but idc
            height *= calculateH(remainingTime)

            y -= height + 3
        }
    }

    /*
    private fun calculateX(time: Long, width: Double): Double {
        return when {
            time in 100L..250L -> (250L - time) / 150.0 * (width + 2) // animation in
            time < 100L -> Double.MAX_VALUE // animation out
            else -> 0.0
        }
    }
     */

    private fun calculateX(time: Long, width: Double): Double {
        return when {
            time in 100L..250L -> {
                val progress = (250L - time) / 150.0
                Easing.easeOutBack(progress) * (width + 2) //change this for diff animation
            }
            time < 100L -> Double.MAX_VALUE // animation out
            else -> 0.0
        }
    }

    private fun calculateH(time: Long): Double {
        return if (time < 100L) {
            val progress = time / 100.0
            Easing.easeInQuad(progress) //change this for diff animation
        } else 1.0
    }

    //add functions for easing here
    object Easing {
        fun easeOutQuad(t: Double): Double = t * (2 - t)
        fun easeInQuad(t: Double): Double = t * t
        fun easeInOutQuad(t: Double): Double = if (t < 0.5) 2 * t * t else -1 + (4 - 2 * t) * t

        fun easeOutBack(t: Double): Double {
            val c1 = 1.70158;
            val c3 = c1 + 1;

            return 1 + c3 * (t - 1).pow(3) + c1 * (t - 1).pow(2);
        }
    }

    /*
    private fun calculateH(time: Long): Double {
        return if (time < 100L) time / 100.0 else 1.0 // animation down
    }
     */
}