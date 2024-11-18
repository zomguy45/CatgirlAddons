package catgirlroutes.module.impl.dungeons

import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ChatUtils
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ScanTest : Module(
    "Scan Test",
    Category.DUNGEON,
    "ODON CLINT SCAN TEST"
){
    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent ) {
        if (event.room == null) return;

        val room: Room = event.room!!;
        ChatUtils.chatMessage("------------");
        ChatUtils.chatMessage("ROOM: " + room.data.name);
        ChatUtils.chatMessage("ROT: " + room.rotation);
        ChatUtils.chatMessage("CLAY: " + room.clayPos);
        ChatUtils.chatMessage("------------");
    }
}