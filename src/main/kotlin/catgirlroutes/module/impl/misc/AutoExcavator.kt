package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.impl.misc.AutoExcavator.cwid
import catgirlroutes.module.impl.misc.AutoExcavator.phase
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.ChatUtils.devMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.EntityAura.entityArray
import catgirlroutes.utils.PlayerUtils.rightClick
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoExcavator : Module(
    name = "Auto Excavator",
    category = Category.MISC
) {
    val clickDelay = NumberSetting("Click Delay", 150.0, 0.0, 300.0, 10.0)
    init {
        this.addSettings(
            clickDelay
        )
    }
    var phase = 2
    var menuOne = mutableListOf<Int>()
    var menuTwo = mutableListOf<Int>()
    var cwid = -1
    var scrapFound = false
    var chiselFound = false
    var lastClick = System.currentTimeMillis()
    var shouldReopen = true

    @SubscribeEvent
    fun onS2D(event: PacketReceiveEvent) {
        if (!this.enabled) return
        if (event.packet !is S2DPacketOpenWindow) return
        val title = event.packet.windowTitle.unformattedText
        cwid = event.packet.windowId

        if (!title.contains("Fossil")) return

        scrapFound = false
        chiselFound = false
        menuOne.clear()
        menuTwo.clear()

        if (phase == 2) {
            phase = 1
            menuOne.add(31)
            devMessage("phase: " + phase)
        } else if (phase == 1) {
            phase = 2
            devMessage("phase: " + phase)
        }
    }

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return

        val slot = event.packet.func_149173_d()
        val itemStack = event.packet.func_149174_e()

        val item = itemStack?.item
        val name = itemStack?.displayName //Dirt
        val metadata = itemStack?.metadata
        val registryName = item?.registryName //minecraft:stained_glass_pane
        val unlocalizedName = item?.unlocalizedName //tile.thinStainedGlass

        if (registryName == "minecraft:stained_glass_pane") {
            if (metadata == 12) {
                menuTwo.add(slot)
            } else if (metadata == 5) {
                menuTwo.add(0, slot)
            }
            if (metadata == 4) {
                if (!shouldReopen) return
                shouldReopen = false
                menuTwo.clear()
                cwid = -1
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
                scheduleTask(0) {
                    rightClick()
                }
                scheduleTask(5) {
                    shouldReopen = true
                }
            }
        }
        if (name?.contains("Chisel") == true&& slot > 53) {
            if (chiselFound) return
            chiselFound = true
            devMessage("Chisel found")
            menuOne.add(0, slot)
        }
        if (name?.contains("Scrap") == true && slot > 53) {
            if (scrapFound) return
            scrapFound = true
            menuOne.add(0, slot)
            devMessage("Scrap found")
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (lastClick + clickDelay.value > System.currentTimeMillis()) return
        lastClick = System.currentTimeMillis()
        if (phase == 1) {
            if (!scrapFound || !chiselFound) {
                phase = 2
                modMessage("No scrap or chisel found!")
                menuOne.clear()
                menuTwo.clear()
                return
            }
            if (menuOne.isEmpty()) return
            click(menuOne.removeFirst())
        }
        if (phase == 2) {

            if (menuTwo.isEmpty()) return
            click(menuTwo.removeFirst())
        }
    }

    override fun onKeyBind() {
        rightClick()
    }

    @SubscribeEvent
    fun onS2DNigger(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        cwid = event.packet.windowId
    }

    @SubscribeEvent
    fun onCloseServer(event: PacketReceiveEvent) {
        if (event.packet !is S2EPacketCloseWindow) return
        cwid = -1
        phase = 2
    }

    @SubscribeEvent
    fun onCloseClient(event: PacketReceiveEvent) {
        if (event.packet !is C0DPacketCloseWindow) return
        cwid = -1
        phase = 2
    }

    fun click(slot: Int) {
        if (cwid == -1) return
        mc.netHandler.addToSendQueue(C0EPacketClickWindow(cwid, slot, 0, 0, null, 0))
    }
}