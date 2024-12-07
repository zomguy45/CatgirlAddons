package catgirlroutes.utils

import catgirlroutes.utils.PacketUtils.sendPacket
import catgirlroutes.utils.Utils.distanceToPlayer
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object EntityAura {

    val entityArray = mutableListOf<Entity>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || entityArray.isEmpty()) return
        entityArray.forEach{entity ->
            if (distanceToPlayer(entity.posX, entity.posY, entity.posZ) < 4) {
                sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT))
                entityArray.remove(entity)
                return
            }
        }
    }
}

