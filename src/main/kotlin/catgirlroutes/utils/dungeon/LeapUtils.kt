package catgirlroutes.utils.dungeon

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.PlayerUtils.airClick
import catgirlroutes.utils.PlayerUtils.swapFromName
import catgirlroutes.utils.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LeapUtils {
    var leapQueue = mutableListOf<String>()
    var menuOpened = false
    var inProgress = false
    var clickedLeap = false
    var cwid = -1

    fun inQueue(): Boolean {
        return leapQueue.size > 0
    }

    fun currentLeap(): String {
        return leapQueue[0]
    }

    fun reloadGui() {
        menuOpened = false
        leapQueue.removeFirst()
        inProgress = false
    }

    fun queueLeap(name: String) {
        leapQueue.add(name)
    }

    fun click(slot: Int) {
        if (cwid == -1) return
        mc.netHandler.addToSendQueue(C0EPacketClickWindow(cwid, slot, 0, 0, null, 0))
    }

    fun leap(name: String) {
        if (!inDungeons) return
        if (inProgress) return
        if (name == "None") return
        inProgress = true
        dungeonTeammatesNoSelf.forEach{ teammate ->
            if (teammate.name != name) return@forEach
            val state = swapFromName("Infinileap")
            if (state == "SWAPPED") {
                scheduleTask(0) {
                    airClick()
                    clickedLeap = true
                }
            } else if (state == "ALREADY_HELD") {
                airClick()
                clickedLeap = true
            }
            leapQueue.add(name)
            devMessage(leapQueue[0])
            return
        }
        inProgress = false
        modMessage("$name not found")
    }

    fun leap(clazzz: DungeonClass) {
        if (!inDungeons || clazzz == DungeonClass.Unknown) return
        if (inProgress) return
        inProgress = true
        dungeonTeammatesNoSelf.forEach{ teammate ->
            if (teammate.clazz == clazzz) {
                val state = swapFromName("Infinileap")
                if (state == "SWAPPED") {
                    scheduleTask(0) {
                        airClick()
                        clickedLeap = true
                    }
                } else if (state == "ALREADY_HELD") {
                    airClick()
                    clickedLeap = true
                }
                leapQueue.add(teammate.name)
                devMessage(leapQueue[0])
                return
            }
        }
        inProgress = false
        modMessage("$clazzz not found")
    }

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return
        if (!inQueue() || !menuOpened) return
        devMessage("5")

        val slot = event.packet.func_149173_d()
        val itemStack = event.packet.func_149174_e()

        if (itemStack == null) return
        devMessage("6")
        if (slot > 35) {
            modMessage(currentLeap() + " not found!")
            reloadGui()
            return
        }
        devMessage("7")
        event.isCanceled = true

        devMessage(itemStack.displayName)
        if (itemStack.displayName.contains(currentLeap())) {
            click(slot)
            reloadGui()
            devMessage("8")
        }
    }

    @SubscribeEvent
    fun onS2D(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        cwid = event.packet.windowId
        devMessage("1")
        if (!inQueue()) return
        devMessage("2")
        val title = event.packet.windowTitle.unformattedText
        devMessage(title)
        if (!title.contains("Leap")) return
        devMessage("3")
        menuOpened = true
        clickedLeap = false
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onCloseServer(event: PacketReceiveEvent) {
        if (event.packet !is S2EPacketCloseWindow) return
        cwid = -1
    }

    @SubscribeEvent
    fun onCloseClient(event: PacketSentEvent) {
        if (event.packet !is C0DPacketCloseWindow) return
        cwid = -1
    }
}