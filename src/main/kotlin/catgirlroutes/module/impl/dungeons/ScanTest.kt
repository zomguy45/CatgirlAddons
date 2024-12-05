package catgirlroutes.module.impl.dungeons

import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.dungeon.tiles.Room
import catgirlroutes.utils.Notifications
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ScanTest : Module(
    "Scan Test",
    Category.DUNGEON,
    "ODON CLINT SCAN TEST"
) {

    private val notif = ActionSetting("notif", "nofiification") { Notifications.send("Test", "I think this is a test") }

    init {
        addSettings(notif)
    }

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