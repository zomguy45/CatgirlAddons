package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.notification.Notification
import catgirlroutes.ui.notification.NotificationType
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

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

    private fun calculateX(time: Long, width: Double): Double {
        return when {
            time in 100L..250L -> (250L - time) / 150.0 * (width + 2) // animation in
            time < 100L -> Double.MAX_VALUE // animation out
            else -> 0.0
        }
    }

    private fun calculateH(time: Long): Double {
        return if (time < 100L) time / 100.0 else 1.0 // animation down
    }
}