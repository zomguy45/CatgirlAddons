package catgirlroutes.utils

import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.PacketUtils.sendPacket
import catgirlroutes.utils.Utils.distanceToPlayer
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object EntityAura {

    val entityArray = mutableListOf<Entity>()
    val recentClicks = mutableListOf<Entity>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || entityArray.isEmpty()) return
        entityArray.forEach{entity ->
            if (distanceToPlayer(entity.posX, entity.posY, entity.posZ) < 4) {
                sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT))
                recentClicks.add(entity)
                scheduleTask(2) {recentClicks.remove(entity)}
                entityArray.remove(entity)
                return
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        recentClicks.forEach{entity ->
            drawBoxByEntity(entity, Color.PINK, entity.width, entity.height)
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        entityArray.clear()
    }
}

