package catgirlroutes.module.impl.dungeons

import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.dungeon.tiles.Room
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ScanTest : Module(
    "Scan Test",
    Category.DUNGEON,
    "ODON CLINT SCAN TEST"
) {
    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        if (event.room == null) return;

        val room: Room = event.room;
        ChatUtils.chatMessage("------------");
        ChatUtils.chatMessage("ROOM: " + room.data.name);
        ChatUtils.chatMessage("ROT: " + room.rotation);
        ChatUtils.chatMessage("CLAY: " + room.clayPos);
        ChatUtils.chatMessage("------------");
    }
}