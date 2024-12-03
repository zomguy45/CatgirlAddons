package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.StringSelectorSetting
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.sidebarLines
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.text.Regex

//TODO: Change the lines that are being checked to the right ones.
//      Add a way to select the player names.
//      Make the triggers for the auto leap.
//      Add S2D cancel.
//      Add fail safes.

object AutoLeap : Module(
    name = "Auto Leap",
    category = Category.DUNGEON
) {
    private val usePlayerName = BooleanSetting("Use playernames",false)
    private val s1target = StringSelectorSetting("Target S1", "A", arrayListOf("A", "B", "M", "H", "T"))
    private val s2target = StringSelectorSetting("Target S2", "A", arrayListOf("A", "B", "M", "H", "T"))
    private val s3target = StringSelectorSetting("Target S3", "A", arrayListOf("A", "B", "M", "H", "T"))
    private val s4target = StringSelectorSetting("Target S4", "A", arrayListOf("A", "B", "M", "H", "T"))
    init {
        this.addSettings(
            usePlayerName,
            s1target,
            s2target,
            s3target,
            s4target,
        )
    }
    var cwid = -1
    var awaitingLeap = false
    var currentTarget = ""
    var playerList = mutableListOf<Map<String, String>>()

    fun leap(target: String) {
        var i = 0
        playerList.clear()
        if (usePlayerName.enabled) {
            currentTarget = target
            awaitingLeap = true
        } else {
            while (i < 5){
                val regex = Regex("""\[([A-Z])\]\s+(.+?)\s""")
                val match = regex.find(sidebarLines[i])
                if (match != null) {
                    val playerClass = match.groupValues[1]
                    val playerName = match.groupValues[2]
                    playerList.add(mapOf("playerClass" to playerClass, "playerName" to playerName))
                }
                i += 1
            }

            for (player in playerList) {
                if (player["playerClass"] == target) {
                    currentTarget == player["playerName"]
                    modMessage(currentTarget)
                    awaitingLeap = true
                }
            }
        }
    }

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return
        if (!awaitingLeap) return
        val item = event.packet.func_149174_e()
        val slot = event.packet.func_149173_d()
        if (item.unformattedName == currentTarget) {
            click(slot)
            awaitingLeap = false
        }
    }

    @SubscribeEvent
    fun onS2d(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        cwid = event.packet.windowId
    }

    fun click(slot: Int) {
        if (cwid == -1) return
        mc.netHandler.addToSendQueue(C0EPacketClickWindow(cwid, slot, 0, 0, null, 0))
    }
}