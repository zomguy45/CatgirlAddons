package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes
import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.AlwaysActive
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.Visibility
import catgirlroutes.module.settings.impl.*
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * Settings for the CLick Gui
 * @author Aton
 */
@AlwaysActive
object ClickGui: Module(
    "ClickGUI",
    Keyboard.KEY_RSHIFT,
    category = Category.RENDER,
    description = "Appearance settings for the click gui. \n" +
            "You can set a custom chat prefix with formatting here. For formatting use & or the paragrph symbol followed by a modifier. " +
            "A benefit of using the paragraph symbol is, that you can directly see how it will look in the text field, but you wont be able to see the formatting. \n" +
            "§00...§ff§r are colors, l is §lBold§r, n is §nUnderlined§r, o is §oItalic§r, m is §mStrikethrough§r, k is §kObfuscated§r, r is Reset."
) {

    val design: StringSelectorSetting
    val blur: BooleanSetting = BooleanSetting("Blur", false,  "Toggles the background blur for the gui.")
    val color = ColorSetting("Color", Color(255,200,0), false, "Color theme in the gui.")
    val colorSettingMode = StringSelectorSetting("Color Mode", "HSB", arrayListOf("HSB", "RGB"), "Mode for all color settings in the gui. Changes the way colors are put in.")
    val toggleNotification: BooleanSetting = BooleanSetting("Notifications", false,  "Send notifications for toggling modules.")

    val clientName: StringSetting = StringSetting("Name", "CatgirlAddons", description = "Name that will be rendered in the gui.")
    val prefixStyle: StringSelectorSetting = StringSelectorSetting("Prefix Style", "Long", arrayListOf("Long", "Short", "Custom"), "Chat prefix selection for mod messages.")
    val customPrefix = StringSetting("Custom Prefix", "§0§l[§4§lCatgirlAddons§0§l]§r", 40,  "You can set a custom chat prefix that will be used when Custom is selected in the Prefix Style dropdown.")

    private val devSettings: BooleanSetting = BooleanSetting("Dev Settings", false, "Expands Developer settings", Visibility.CLICK_GUI_ONLY)
    val devMode: BooleanSetting = BooleanSetting("Dev Mode", false, "Toggles developer mode").withDependency { devSettings.enabled }
    val debugMode: BooleanSetting = BooleanSetting("Debug Mode", false, "Toggles debug mode").withDependency { devSettings.enabled }
    val forceHypixel: BooleanSetting = BooleanSetting("Force Hypixel", false, "Makes the mod think that you're on Hypixel").withDependency { devSettings.enabled }
    val forceSkyblock: BooleanSetting = BooleanSetting("Force Skyblock", false, "Makes the mod think that you're in Skyblock").withDependency { devSettings.enabled }
    val forceDungeon: BooleanSetting = BooleanSetting("Force Dungeon", false, "Makes the mod think that you're in Dungeon").withDependency { devSettings.enabled }

    val showUsageInfo = BooleanSetting("Usage Info", true, "Show info on how to use the GUI.", Visibility.ADVANCED_ONLY)

    val panelX: MutableMap<Category, NumberSetting> = mutableMapOf()
    val panelY: MutableMap<Category, NumberSetting> = mutableMapOf()
    val panelExtended: MutableMap<Category, BooleanSetting> = mutableMapOf()

    private const val pwidth = 120.0
    private const val pheight = 15.0

    val panelWidth: NumberSetting  = NumberSetting("Panel width", pwidth, visibility = Visibility.HIDDEN)
    val panelHeight: NumberSetting = NumberSetting("Panel height", pheight, visibility = Visibility.HIDDEN)

    const val advancedRelWidth = 0.5
    const val advancedRelHeight = 0.5

    val advancedRelX = NumberSetting("Advanced_RelX",(1 - advancedRelWidth)/2.0,0.0, (1- advancedRelWidth), 0.0001, visibility = Visibility.HIDDEN)
    val advancedRelY = NumberSetting("Advanced_RelY",(1 - advancedRelHeight)/2.0,0.0, (1- advancedRelHeight), 0.0001, visibility = Visibility.HIDDEN)

    init {
        val options = java.util.ArrayList<String>()
        options.add("JellyLike")
        options.add("New")
        design = StringSelectorSetting("Design","JellyLike", options, "Design theme of the gui.")

        addSettings(
            design,
            blur,
            color,
            colorSettingMode,
            toggleNotification,

            clientName,
            prefixStyle,
            customPrefix,

            devSettings,
            devMode,
            debugMode,
            forceHypixel,
            forceSkyblock,
            forceDungeon,

            showUsageInfo,
            advancedRelX,
            advancedRelY
        )

        // The Panels

        // this will set the default click gui panel settings. These will be overwritten by the config once it is loaded
        resetPositions()

        addSettings(
            panelWidth,
            panelHeight
        )

        for(category in Category.entries) {
            addSettings(
                panelX[category]!!,
                panelY[category]!!,
                panelExtended[category]!!
            )
        }
    }

    /**
     * Adds if missing and sets the default click gui positions for the category panels.
     */
    private fun resetPositions() {
        panelWidth.value = pwidth
        panelHeight.value = pheight

        var px = 10.0
        val py = 10.0
        val pxplus = panelWidth.value + 10
        for(category in Category.entries) {
            panelX.getOrPut(category) { NumberSetting(category.name + ",x", px, visibility = Visibility.HIDDEN) }.value = px
            panelY.getOrPut(category) { NumberSetting(category.name + ",y", py, visibility = Visibility.HIDDEN) }.value = py
            panelExtended.getOrPut(category) { BooleanSetting(category.name + ",extended", true, visibility = Visibility.HIDDEN) }.enabled = true
            px += pxplus
        }

        advancedRelX.reset()
        advancedRelY.reset()
    }

    /**
     * Overridden to prevent the chat message from being sent.
     */
    override fun onKeyBind() {
        display = CatgirlRoutes.clickGUI
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        display = CatgirlRoutes.clickGUI
        super.onEnable()
        toggle()
    }
}