package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.clickgui.util.FontUtil.fontHeight
import catgirlroutes.utils.ChatUtils.command
import catgirlroutes.utils.render.HUDRenderUtils.sr
import catgirlroutes.utils.renderText
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object AutoWardrobe : Module(
    name = "Auto Wardrobe",
    category = Category.MISC
) {
    private val wd1 = KeyBindSetting("Wardrobe 1", Keyboard.KEY_NONE, "...").onPress { targetSlot = 36; openMenu() }
    private val wd2 = KeyBindSetting("Wardrobe 2", Keyboard.KEY_NONE, "...").onPress { targetSlot = 37; openMenu() }
    private val wd3 = KeyBindSetting("Wardrobe 3", Keyboard.KEY_NONE, "...").onPress { targetSlot = 38; openMenu() }
    private val wd4 = KeyBindSetting("Wardrobe 4", Keyboard.KEY_NONE, "...").onPress { targetSlot = 39; openMenu() }
    private val wd5 = KeyBindSetting("Wardrobe 5", Keyboard.KEY_NONE, "...").onPress { targetSlot = 40; openMenu() }
    private val wd6 = KeyBindSetting("Wardrobe 6", Keyboard.KEY_NONE, "...").onPress { targetSlot = 41; openMenu() }
    private val wd7 = KeyBindSetting("Wardrobe 7", Keyboard.KEY_NONE, "...").onPress { targetSlot = 42; openMenu() }
    private val wd8 = KeyBindSetting("Wardrobe 8", Keyboard.KEY_NONE, "...").onPress { targetSlot = 43; openMenu() }
    private val wd9 = KeyBindSetting("Wardrobe 9", Keyboard.KEY_NONE, "...").onPress { targetSlot = 44; openMenu() }

    init {
        this.addSettings(
            wd1, wd2, wd3, wd4 , wd5, wd6, wd7, wd8, wd9
        )
    }
    var active = false
    var targetSlot = 0
    var cwid = -1

    private fun openMenu() {
        active = true
        command("wd", false)
    }

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !active || mc.ingameGUI == null) return
        renderText("§5[§dEquipping WD:§c ${targetSlot - 35}§5]")
    }

    @SubscribeEvent
    fun onS2D(event: PacketReceiveEvent) {
        if (event.packet !is S2DPacketOpenWindow) return
        val title = event.packet.windowTitle.unformattedText
        cwid = event.packet.windowId

        if (!title.contains("Wardrobe") || !active) return
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onS2F(event: PacketReceiveEvent) {
        if (event.packet !is S2FPacketSetSlot) return
        if (!this.enabled) return
        if (!active) return
        val slot = event.packet.func_149173_d()
        if (slot == targetSlot) {
            click(slot)
            active = false
            mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            cwid = -1
        }
        if (slot > 45) {
            cwid = -1
            active = false
        }
    }

    fun click(slot: Int) {
        if (cwid == -1) return
        mc.netHandler.addToSendQueue(C0EPacketClickWindow(cwid, slot, 0, 0, null, 0))
    }
}
