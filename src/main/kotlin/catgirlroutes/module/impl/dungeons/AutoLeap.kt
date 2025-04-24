package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.ChatPacket
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.render.ClickGui
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.*
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.PlayerUtils.posX
import catgirlroutes.utils.PlayerUtils.posY
import catgirlroutes.utils.PlayerUtils.posZ
import catgirlroutes.utils.dungeon.DungeonClass
import catgirlroutes.utils.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import catgirlroutes.utils.dungeon.DungeonUtils.inBoss
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.dungeon.LeapUtils.leap
import catgirlroutes.utils.noControlCodes
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

//TODO: Change the lines that are being checked to the right ones.
//      Add a way to select the player names.
//      Make the triggers for the auto leap.
//      Add S2D cancel.
//      Add fail safes.

object AutoLeap : Module( // todo improve selectors
    "Auto Leap",
    Category.DUNGEON,
    tag = TagType.WHIP
) {
    private val fastLeap by BooleanSetting("Fast leap", "Leaps to a set player on infinileap left click.")
    private val autoLeap by BooleanSetting("Auto leap", "Automatically leaps to a set player.")

    private var leapMode by SelectorSetting("Leap mode", "Name", arrayListOf("Name", "Class"), "Leap mode for the module.")
    private var teamList by ListSetting("Teammates", mutableListOf("None"))

    private var clearLeap by SelectorSetting("Clear leap", "None", ArrayList(teamList), "Player name to leap to during clear.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s1leap by SelectorSetting("S1 leap", "None", ArrayList(teamList), "Player name to leap to when in S1.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s2leap by SelectorSetting("S2 leap", "None", ArrayList(teamList), "Player name to leap to when in S2.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s3leap by SelectorSetting("S3 leap", "None", ArrayList(teamList), "Player name to leap to when in S3.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var s4leap by SelectorSetting("S4 leap", "None", ArrayList(teamList), "Player name to leap to when in S4.").withDependency { isFlopper && leapMode.selected == "Name" }
    private var update by ActionSetting("Update", "Updates teammates names (currently works in dungeon only)..") { updateTeammates() }.withDependency { isFlopper && leapMode.selected == "Name" }

    private val clazzArray = arrayListOf("Mage", "Bers", "Arch", "Tank", "Heal", "None")
    private var clearLeapClass by SelectorSetting("Clear leap", "None", clazzArray, "Player class to leap to during clear.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s1leapClass by SelectorSetting("S1 leap", "None", clazzArray, "Player name to leap to when in S1.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s2leapClass by SelectorSetting("S2 leap", "None", clazzArray, "Player name to leap to when in S2.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s3leapClass by SelectorSetting("S3 leap", "None", clazzArray, "Player name to leap to when in S3.").withDependency { isFlopper && leapMode.selected == "Class" }
    private var s4leapClass by SelectorSetting("S4 leap", "None", clazzArray, "Player name to leap to when in S4.").withDependency { isFlopper && leapMode.selected == "Class" }

    private val clazzOrder by OrderSetting("Class order", mapOf("S1" to "Archer", "S2" to "Healer", "S3" to "Mage", "S4" to "Mage", "Clear" to "None"), 2, clazzArray).withDependency { !isFlopper && leapMode.selected == "Class" }

    private val namesOrder by OrderSetting("Name order", mapOf("S1" to "None", "S2" to "None", "S3" to "None", "S4" to "None", "Clear" to "None"), 2, teamList) {
        updateTeammates()
    }.withDependency { !isFlopper && leapMode.selected == "Name" }

    private val classEnumMapping = arrayListOf(DungeonClass.Mage, DungeonClass.Berserk, DungeonClass.Archer, DungeonClass.Tank, DungeonClass.Healer, DungeonClass.Unknown)

    private val isFlopper: Boolean get() = ClickGui.clickGui.selected == "Flopper"

    init {
        listOf(clearLeap, s1leap, s2leap, s3leap, s4leap).forEach { it.options = teamList }
    }

    private fun updateTeammates() {
        if (dungeonTeammatesNoSelf.isEmpty()) return

        val teammates = dungeonTeammatesNoSelf.map { it.name } + "None"
        teamList = teammates.toMutableList()
        listOf(clearLeap, s1leap, s2leap, s3leap, s4leap).forEach { it.options = teammates }
    }

    /*
    s1 min (89, 100, 48) max (113, 160, 122)
    s2 min (19, 100, 121) max (91, 160, 145)
    s3 min (-6, 100, 50) max (19, 160, 123)
    s4 min (17, 100, 27) max (90, 160, 50)
     */

    @SubscribeEvent
    fun onMouse(event: MouseEvent) {
        if (!event.buttonstate || event.button != 0 || !inDungeons || !fastLeap) return
        if (mc.thePlayer?.heldItem?.displayName?.noControlCodes != "Infinileap") return
        handleLeap()
    }

    @SubscribeEvent
    fun onChat(event: ChatPacket) {
        if (!inDungeons || !inBoss || !autoLeap) return
        val message = event.message
        if ("(7/7)" in message || "(8/8)" in message) {
            handleLeap()
        }
    }

    data class LeapThing(val key: String, val name: String, val classIndex: Int)

    private fun stupid(key: String): LeapThing =
        LeapThing(
            key,
            if (isFlopper) s1leap.selected else namesOrder[key].toString(),
            if (isFlopper) s1leapClass.index else clazzArray.indexOf(clazzOrder[key])
        )

    private fun handleLeap() {
        val thing = when {
            posX in 89.0..113.0 && posY in 100.0..160.0 && posZ in 48.0..122.0 -> stupid("S1")
            posX in 19.0..91.0 && posY in 100.0..160.0 && posZ in 121.0..145.0 -> stupid("S2")
            posX in -6.0..19.0 && posY in 100.0..160.0 && posZ in 50.0..123.0 -> stupid("S3")
            posX in 17.0..90.0 && posY in 100.0..160.0 && posZ in 27.0..50.0 -> stupid("S4")
            !inBoss -> stupid("Clear")
            else -> return
        }

        debugMessage("leaping to ${thing.name} ${classEnumMapping.getOrNull(thing.classIndex)}")

        when (leapMode.selected) {
            "Name" -> leap(thing.name)
            "Class" -> leap(classEnumMapping.getOrElse(thing.classIndex) { DungeonClass.Unknown })
        }
    }
}