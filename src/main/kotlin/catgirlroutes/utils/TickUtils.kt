package catgirlroutes.utils

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

data class ScheduledTask(
    var ticksRemaining: Int,
    val action: () -> Unit
)

object TickUtils {
    private val tasks = mutableListOf<ScheduledTask>()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }
    fun delayInTicks(delay: Int, action: () -> Unit) {
        tasks.add(ScheduledTask(delay, action))
    }
    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            val iterator = tasks.iterator()
            while (iterator.hasNext()) {
                val task = iterator.next()
                task.ticksRemaining--
                if (task.ticksRemaining <= 0) {
                    task.action()
                    iterator.remove()
                }
            }
        }
    }
}
