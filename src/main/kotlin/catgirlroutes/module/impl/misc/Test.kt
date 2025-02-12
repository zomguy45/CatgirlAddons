package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.clickGUINew
import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.ui.notification.NotificationType
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.Notifications
import catgirlroutes.utils.dungeon.tiles.Room
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Test : Module(
    "Test",
    Category.MISC,
    "MODULE FOR VARIOUS TESTS",
    TagType.HARAM
) {
    private val notifDropDown = DropdownSetting("Drop down", false)
    private val notif = ActionSetting("notif", "nofiification") { Notifications.send("Info", "I think this is an info") }.withDependency { notifDropDown.enabled }
    private val notifWarning = ActionSetting("notifWarning", "nofiification") { Notifications.send("Warning (long title test hello 123 herp me hurrep hai)", "I think this is a warning", 5000.0, NotificationType.WARNING) }.withDependency { notifDropDown.enabled }
    private val notifErr = ActionSetting("notifErr", "nofiification") { Notifications.send("Error", "LONG DESCRIPTION TEST every day for the past month I've heard students non-stop joking about P. diddy, Epstein, R. kelly when a student has to go to take a piss he tells his friend group \"one minute bro i gotta pull an R kelly\" they say \"no diddy\" i think like another version of \"no homo\" one student was messing with the computer spamming the windows error sound and said \"this is how Stephen Hawking was moaning in those kids ears at epsteins island\" I've probably heard \"ain't no party like a diddy party\" a thousand times this month alone, im just tired of it all", type = NotificationType.ERROR, icon = "fallingkittens/bread.png") }.withDependency { notifDropDown.enabled }
    private val notifErr2 = ActionSetting("notifErr", "nofiification") { Notifications.send("Error", "LONG DESCRIPTION TEST every day for the past month I've heard students non-stop joking about P. diddy, Epstein, R. kelly when a student has to go to take a piss he tells his friend group \"one minute bro i gotta pull an R kelly\" they say \"no diddy\" i think like another version of \"no homo\" one student was messing with the computer spamming the windows error sound and said \"this is how Stephen Hawking was moaning in those kids ears at epsteins island\" I've probably heard \"ain't no party like a diddy party\" a thousand times this month alone, im just tired of it all", type = NotificationType.ERROR) }.withDependency { notifDropDown.enabled }
    private val notifIcon = ActionSetting("notifIcon", "nofiification") { Notifications.send("Info", "I think this is an info", icon = "Icon.png") }.withDependency { notifDropDown.enabled }

    private val colourTest = ColorSetting("Colour", Color.BLUE)
    private val colourTest2 = ColorSetting("Colour2", Color.BLUE, false)

    private val slider = NumberSetting("Slider", 20.0, 0.0, 100.0, 5.0, unit = "px")
    private val getHudWidth = ActionSetting("Get hud width") {
        debugMessage(TestHud.width)
    }

    private val newclickGui = ActionSetting("New click gui") {
        display = clickGUINew
    }

    init {
        addSettings(
            this.notifDropDown,
            this.notif,
            this.notifWarning,
            this.notifErr,
            this.notifErr2,
            this.notifIcon,

            this.colourTest,
            this.colourTest2,

            this.slider,
            this.getHudWidth,

            this.newclickGui
        )
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

    @RegisterHudElement
    object TestHud : HudElement(
        this,
        width = mc.fontRendererObj.getStringWidth("Test") + slider.value.toInt(),
        height = mc.fontRendererObj.FONT_HEIGHT + 2
    ) {
        override fun renderHud() {
            FontUtil.drawStringWithShadow("Test", 0.0, 0.0)
        }

        override fun setDimensions() {
            this.width = mc.fontRendererObj.getStringWidth("Test") + slider.value.toInt()
        }
    }

}