package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.notification.Notification
import catgirlroutes.ui.notification.NotificationType
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Notifications {
    private val notifications = mutableListOf<Notification>()

    fun send(title: String, description: String, duration: Double = 2000.0, type: NotificationType = NotificationType.INFORMATION) {
        val notification = Notification(title, description, duration, type)
        notifications.add(notification)
    }

    @SubscribeEvent
    fun onRender(event: TickEvent.RenderTickEvent) { // todo: animations
        if (event.phase != TickEvent.Phase.END) return
        if (notifications.size == 0) return

        notifications.removeIf { it.end <= System.currentTimeMillis() }

        val sr = ScaledResolution(mc)
        var y = sr.scaledHeight - 37.0

        for (notification in notifications) {
            val width = Math.max(150.0, mc.fontRendererObj.getStringWidth(notification.description) + 10.0)
            val height = 35.0

            val x = sr.scaledWidth - width - 2

            notification.draw(x, y, width, height)

            y -= height + 3f
        }
    }
}