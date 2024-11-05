package catgirlroutes.events

import catgirlroutes.utils.Island
import net.minecraftforge.fml.common.eventhandler.Event

class SkyblockJoinIslandEvent(val island: Island) : Event()