package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.events.impl.PacketSentEventReturn
import catgirlroutes.mixins.accessors.AccessorGuiEditSign
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Visibility
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.ui.misc.searchoverlay.AhBzSearch
import catgirlroutes.ui.misc.searchoverlay.OverlayType
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.LocationManager.inSkyblock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.init.Items
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard


object SearchOverlay : Module(
    "Search Overlay",
    Category.MISC,
    tag = TagType.WHIP
) { // todo: neu type shit search overlay (jei shit on the side)

    private val auctionOverlay = BooleanSetting("Auction Overlay")
    private val bazaarOverlay = BooleanSetting("Bazaar Overlay")

    val ahHistory = StringSetting("AH_SEARCH", "[\"\"]",  9999, visibility = Visibility.HIDDEN)
    val bzHistory = StringSetting("BZ_SEARCH", "[\"\"]", 9999, visibility = Visibility.HIDDEN)
    private val ctrlF = BooleanSetting("Ctrl + F to search")

    private var overlay: OverlayType = OverlayType.NONE
    private var clickedSearch = false

    init {
        addSettings(this.auctionOverlay, this.bazaarOverlay, this.ahHistory, this.bzHistory, this.ctrlF)
    }

//    override fun onEnable() {
//        display = BazaarSearchOverlay()
//        toggle()
//        super.onEnable()
//    }

    @SubscribeEvent
    fun onS2DPacketOpenWindow(event: PacketReceiveEvent) {
        if (!inSkyblock || event.packet !is S2DPacketOpenWindow) return
        val title = event.packet.windowTitle.unformattedText
        if ((title.contains("Auctions") || title.contains("Auction House")) && this.auctionOverlay.enabled) {
            overlay = OverlayType.AUCTION
        } else if (title.contains("Bazaar") && this.bazaarOverlay.enabled) {
            overlay = OverlayType.BAZAAR
        }
    }

    @SubscribeEvent
    fun onC0EPacketClickWindow(event: PacketSentEvent) {
        if (!inSkyblock || overlay == OverlayType.NONE || event.packet !is C0EPacketClickWindow) return
        val registry = event.packet.clickedItem?.item?.registryName
        val name = event.packet.clickedItem?.displayName
        val slot = event.packet.slotId
        clickedSearch = (registry == "minecraft:sign" && name == "§aSearch" && slot in listOf(48, 45))
        debugMessage(clickedSearch)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onGuiScreenKeyboard(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (event.gui !is GuiChest || overlay == OverlayType.NONE || !this.ctrlF.enabled) return
        val openSlots = mc.thePlayer?.openContainer?.inventorySlots ?: return

        val slotId = if (overlay == OverlayType.AUCTION) 48 else 45
        val gui = event.gui as GuiChest
        val signStack = openSlots[slotId]?.stack

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_F)) {
            if (signStack?.item == Items.sign && signStack?.displayName == "§aSearch") {
                mc.playerController.windowClick(gui.inventorySlots.windowId, slotId, 0, 0, mc.thePlayer)
            } else {
//                display = AhBzSearch(overlay) // todo: fix
            }
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiEditSign || overlay == OverlayType.NONE || !clickedSearch) return

        val sign = (event.gui as AccessorGuiEditSign).tileSign
        sign?.let {
            event.gui = AhBzSearch(overlay, sign)
        }
    }
}