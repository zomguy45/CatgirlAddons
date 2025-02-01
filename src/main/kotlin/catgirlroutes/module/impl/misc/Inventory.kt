package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.impl.PacketReceiveEvent
import catgirlroutes.events.impl.PacketSentEvent
import catgirlroutes.mixins.accessors.AccessorGuiEditSign
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.Visibility
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.module.settings.impl.StringSetting
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.ui.misc.elements.impl.MiscElementText
import catgirlroutes.ui.misc.searchoverlay.AhBzSearch
import catgirlroutes.ui.misc.searchoverlay.OverlayType
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.LocationManager.inSkyblock
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse


object Inventory : Module(
    "Inventory",
    Category.MISC,
    "Various quality of life features for inventory GUIs",
    tag = TagType.WHIP
) { // todo: neu type shit search overlay (jei shit on the side)

    private val searchBar = BooleanSetting("Search bar")
    private val itemList = BooleanSetting("Item list").withDependency { searchBar.enabled }

    private val ahDropdown = DropdownSetting("Auction house")
    private val auctionOverlay = BooleanSetting("Auction search").withDependency { ahDropdown.enabled }

    private val bzDropdown = DropdownSetting("Bazaar")
    private val bazaarOverlay = BooleanSetting("Bazaar search").withDependency { bzDropdown.enabled }

    private val ctrlF = BooleanSetting("Ctrl + F to search") // todo: Make it click on signs in other guis (price, quantity, etc)

    val ahHistory = StringSetting("AH_SEARCH", "[\"\"]",  5000, visibility = Visibility.HIDDEN)
    val bzHistory = StringSetting("BZ_SEARCH", "[\"\"]", 5000, visibility = Visibility.HIDDEN)
    val barX = NumberSetting("SEARCH_BAR_X", visibility = Visibility.HIDDEN)
    val barY = NumberSetting("SEARCH_BAR_Y", visibility = Visibility.HIDDEN)
    val barScale = NumberSetting("SEARCH_BAR_SCALE", 1.0,0.1,4.0,0.02, visibility = Visibility.HIDDEN)

    private var overlay: OverlayType = OverlayType.NONE
    private var clickedSearch = false

    private var bar = MiscElementText(width = 250.0, height = 25.0) // temp

    init {
        addSettings(
            this.searchBar,
            this.itemList,

            this.ahDropdown,
            this.auctionOverlay,

            this.bzDropdown,
            this.bazaarOverlay,

            this.ctrlF,

            this.ahHistory,
            this.bzHistory,
            this.barX,
            this.barY,
            this.barScale
        )
    }

    @SubscribeEvent
    fun onDrawScreenPost(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (!inSkyblock || !this.searchBar.enabled ||  mc.currentScreen !is GuiInventory) return // mc.currentScreen !is GuiChest
        GlStateManager.pushMatrix()
        GlStateManager.scale(barScale.value, barScale.value, barScale.value)

        bar.apply {
            x = barX.value
            y = barY.value
            render(0, 0)
        }

        GlStateManager.popMatrix()
    }

    @SubscribeEvent
    fun onGuiScreenKeyboard2(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (!inSkyblock || !this.searchBar.enabled ||  mc.currentScreen !is GuiInventory) return
        bar.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())
    }

    @SubscribeEvent
    fun onGuiScreenMouse(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!inSkyblock || !this.searchBar.enabled ||  mc.currentScreen !is GuiInventory) return
        bar.mouseClicked(Mouse.getX(), Mouse.getY(), Mouse.getEventButton())
    }

    // dummy HudElement to move search bar position
    @RegisterHudElement
    object SearchBar : HudElement(this, barX, barY, 250, 25, barScale) { override fun renderHud() {  } }

    @SubscribeEvent
    fun onS2DPacketOpenWindow(event: PacketReceiveEvent) {
        if (!inSkyblock || event.packet !is S2DPacketOpenWindow) return
        val title = event.packet.windowTitle.unformattedText
        overlay = if ((title.contains("Auctions") || title.contains("Auction House")) && this.auctionOverlay.enabled) {
            OverlayType.AUCTION
        } else if (title.contains("Bazaar") && this.bazaarOverlay.enabled) {
            OverlayType.BAZAAR
        } else {
            OverlayType.NONE
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