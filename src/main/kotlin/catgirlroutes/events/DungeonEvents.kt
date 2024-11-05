package catgirlroutes.events


import catgirlroutes.utils.dungeon.tiles.FullRoom
import net.minecraftforge.fml.common.eventhandler.Event

abstract class DungeonEvents : Event() {
    class RoomEnterEvent(val fullRoom: FullRoom?) : DungeonEvents()
}