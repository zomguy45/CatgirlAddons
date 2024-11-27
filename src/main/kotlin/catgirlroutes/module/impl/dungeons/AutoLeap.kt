package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import me.odinmain.utils.sidebarLines
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AutoLeap : Module(
    name = "Auto Leap",
    category = Category.DUNGEON
) {
    var cwid = -1
    var awaitingLeap = false
    var playerName = "SERENITYISSBLACK"
    var playerList = mutableListOf<String>()

    override fun onKeyBind() {
        leap("test")
    }
    fun leap(playerName: String) {
        var i = 0
        modMessage("Debug")
        playerList.clear()
        while (i < 5){
            i += 1
            playerList.add(sidebarLines[i])
            modMessage(sidebarLines.get(i))
        }
    }

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return
        if (!awaitingLeap) return
        val item = event.packet.func_149174_e()
        val slot = event.packet.func_149173_d()
        if (item.unformattedName.startsWith(playerName)) {
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