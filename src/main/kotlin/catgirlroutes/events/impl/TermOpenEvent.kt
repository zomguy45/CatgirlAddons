package catgirlroutes.events.impl

import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraftforge.fml.common.eventhandler.Event

open class TermOpenEvent(val packet: C02PacketUseEntity) : Event()