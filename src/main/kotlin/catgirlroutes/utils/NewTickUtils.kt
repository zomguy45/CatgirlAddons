package catgirlroutes.utils

import catgirlroutes.CatgirlRoutes.Companion.mc
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList

object ClientListener {
    private var ticksPassed: Int = 0
    private val tasks = CopyOnWriteArrayList<Task>()

    class Task(var delay: Int, val callback: () -> Unit)

    init {
        ticksPassed = 0
    }

    fun addTask(delay: Int, callback: () -> Unit) {
        tasks.add(Task(delay, callback))
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) return

        tasks.removeAll {
            if (it.delay-- <= 0) {
                mc.addScheduledTask { it.callback() }
                true
            } else false
        }
        ticksPassed++
    }

    fun scheduleTask(delay: Int = 0, callback: () -> Unit) {
        addTask(delay, callback)
    }
}