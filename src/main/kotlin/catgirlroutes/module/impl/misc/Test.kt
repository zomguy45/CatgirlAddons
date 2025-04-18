package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.RoomEnterEvent
import catgirlroutes.mixins.accessors.IEntityPlayerSPAccessor
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.*
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.ui.notification.NotificationType
import catgirlroutes.utils.*
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.EtherWarpHelper.etherPos
import catgirlroutes.utils.dungeon.tiles.Room
import catgirlroutes.utils.render.WorldRenderUtils
import net.minecraft.block.Block.getIdFromBlock
import net.minecraft.init.Blocks
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.*

object Test : Module(
    "Test",
    Category.MISC,
    "MODULE FOR VARIOUS TESTS",
    TagType.HARAM
) {
    private val notifDropDown = DropdownSetting("Drop down", false)
    private val notif = ActionSetting("notif", "nofiification") { Notifications.send("Info", "I think this is an info") }.withDependency(notifDropDown)
    private val notifWarning = ActionSetting("notifWarning", "nofiification") { Notifications.send("Warning (long title test hello 123 herp me hurrep hai)", "I think this is a warning", 5000.0, NotificationType.WARNING) }.withDependency(notifDropDown)
    private val notifErr = ActionSetting("notifErr", "nofiification") { Notifications.send("Error", "LONG DESCRIPTION TEST every day for the past month I've heard students non-stop joking about P. diddy, Epstein, R. kelly when a student has to go to take a piss he tells his friend group \"one minute bro i gotta pull an R kelly\" they say \"no diddy\" i think like another version of \"no homo\" one student was messing with the computer spamming the windows error sound and said \"this is how Stephen Hawking was moaning in those kids ears at epsteins island\" I've probably heard \"ain't no party like a diddy party\" a thousand times this month alone, im just tired of it all", type = NotificationType.ERROR, icon = "fallingkittens/bread.png") }.withDependency(notifDropDown)
    private val notifErr2 = ActionSetting("notifErr", "nofiification") { Notifications.send("Error", "LONG DESCRIPTION TEST every day for the past month I've heard students non-stop joking about P. diddy, Epstein, R. kelly when a student has to go to take a piss he tells his friend group \"one minute bro i gotta pull an R kelly\" they say \"no diddy\" i think like another version of \"no homo\" one student was messing with the computer spamming the windows error sound and said \"this is how Stephen Hawking was moaning in those kids ears at epsteins island\" I've probably heard \"ain't no party like a diddy party\" a thousand times this month alone, im just tired of it all", type = NotificationType.ERROR) }.withDependency(notifDropDown)
    private val notifIcon = ActionSetting("notifIcon", "nofiification") { Notifications.send("Info", "I think this is an info", icon = "Icon.png") }.withDependency(notifDropDown)

    private val dropdownTest = DropdownSetting("Dropdown test")
    private val stupid1 = BooleanSetting("stupid1").withDependency(dropdownTest)
    private val stupid2 = BooleanSetting("stupid2").withDependency(dropdownTest) { this.stupid1.enabled }
    private val stupid3 = ColorSetting("stupid3", Color.WHITE).withDependency(dropdownTest) { this.stupid1.enabled }

    private val colourTest = ColorSetting("Colour", Color.BLUE)
    private val colourTest2 = ColorSetting("Colour2", Color.BLUE, false)

    private val slider = NumberSetting("Slider", 20.0, 0.0, 100.0, 5.0, unit = "px")
    private val getHudWidth = ActionSetting("Get hud width") {
        debugMessage(TestHud.width)
    }

    private val listTest = ListSetting("Test list", mutableListOf<Stupid>())
    private val stupid4 = ActionSetting("Stupid4") { this.listTest.value.add(Stupid("stupid", 1)) }
    private val stupid5 = ActionSetting("Stupid5") { debugMessage(this.listTest.value) }

    private val mapTest = MapSetting("Test map", mutableMapOf<Double, Stupid>())
    private val stupid6 = ActionSetting("Stupid6") { this.mapTest.value[Math.random()] = Stupid("stupid2", 2) }
    private val stupid7 = ActionSetting("Stupid7") { debugMessage(this.mapTest.value) }

    data class Stupid(val stupid1: String, val stupid2: Int)

    init {
        addSettings(
            this.notifDropDown,
            this.notif,
            this.notifWarning,
            this.notifErr,
            this.notifErr2,
            this.notifIcon,

            this.dropdownTest,
            this.stupid1,
            this.stupid2,
            this.stupid3,

            this.colourTest,
            this.colourTest2,

            this.slider,
            this.getHudWidth,

            this.listTest,
            this.stupid4,
            this.stupid5,

            this.mapTest,
            this.stupid6,
            this.stupid7
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

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (mc.thePlayer?.usingEtherWarp == false) return
        val player = mc.thePlayer as? IEntityPlayerSPAccessor ?: return
        val positionLook = PositionLook(Vec3(player.lastReportedPosX, player.lastReportedPosY, player.lastReportedPosZ), player.lastReportedYaw, player.lastReportedPitch)

        etherPos = EtherWarpHelper.getEtherPos(positionLook)

        val succeeded = etherPos.succeeded && (mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || etherPos.state?.block?.let { invalidBlocks.get(getIdFromBlock(it)) } != true)

        WorldRenderUtils.drawBlock(etherPos.pos ?: return, if (succeeded) Color.GREEN else Color.RED)
    }

    private val invalidBlocks = BitSet().apply {
        setOf(
            Blocks.hopper, Blocks.chest, Blocks.ender_chest, Blocks.furnace, Blocks.crafting_table, Blocks.cauldron,
            Blocks.enchanting_table, Blocks.dispenser, Blocks.dropper, Blocks.brewing_stand, Blocks.trapdoor,
        ).forEach { set(getIdFromBlock(it)) }
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
