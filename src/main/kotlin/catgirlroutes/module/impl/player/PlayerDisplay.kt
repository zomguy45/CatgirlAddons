package catgirlroutes.module.impl.player

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.module.settings.impl.DropdownSetting
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.utils.LocationManager.inSkyblock
import catgirlroutes.utils.SkyblockPlayer
import catgirlroutes.utils.SkyblockPlayer.DEF_REGEX
import catgirlroutes.utils.SkyblockPlayer.HP_REGEX
import catgirlroutes.utils.SkyblockPlayer.MANA_REGEX
import catgirlroutes.utils.SkyblockPlayer.OVERFLOW_REGEX
import catgirlroutes.utils.SkyblockPlayer.SALVATION_REGEX
import catgirlroutes.utils.SkyblockPlayer.STACKS_REGEX
import catgirlroutes.utils.render.HUDRenderUtils.drawOutlinedRectBorder
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.text.NumberFormat
import java.util.*


object PlayerDisplay: Module(
    name = "Player Display",
    category = Category.PLAYER
) {

    private val hideHealth = BooleanSetting("Hide health")
    private val hideArmour = BooleanSetting("Hide armour")
    private val hideHunger = BooleanSetting("Hide hunger")

    private val healthDropdown = DropdownSetting("Health dropdown")
    private val health = BooleanSetting("Health").withDependency { healthDropdown.enabled }
    private val healthColour = ColorSetting("Health colour", Color(255,85,85)).withDependency { healthDropdown.enabled && health.enabled }
    private val healthBar = BooleanSetting("Health bar").withDependency { healthDropdown.enabled }
    private val healthBarColour = ColorSetting("Health bar colour", Color(255,85,85)).withDependency { healthDropdown.enabled && healthBar.enabled }

    private val manaDropdown = DropdownSetting("Mana dropdown")
    private val mana = BooleanSetting("Mana").withDependency { manaDropdown.enabled }
    private val manaColour = ColorSetting("Mana colour", Color(85, 85, 255)).withDependency { manaDropdown.enabled && mana.enabled }
    private val manaBar = BooleanSetting("Mana bar").withDependency { manaDropdown.enabled }
    private val manaBarColour = ColorSetting("Mana bar colour", Color(85, 85, 255)).withDependency { manaDropdown.enabled && manaBar.enabled }

    private val otherDropdown = DropdownSetting("Other")
    private val defence = BooleanSetting("Defence").withDependency { otherDropdown.enabled }
    private val defenceColour = ColorSetting("Defence colour", Color( 85, 255, 85)).withDependency { otherDropdown.enabled && defence.enabled }
    private val effectiveHealth = BooleanSetting("Effective health").withDependency { otherDropdown.enabled }
    private val speed = BooleanSetting("Speed").withDependency { otherDropdown.enabled }
    private val overflowMana = BooleanSetting("Overflow mana").withDependency { otherDropdown.enabled }
    private val stacks = BooleanSetting("Crimson stacks").withDependency { otherDropdown.enabled }
    private val salvation = BooleanSetting("Salvation").withDependency { otherDropdown.enabled }

    private val barWidth = 75.0
    private val barHeight = 7.0

    init {
        addSettings(
            this.hideHealth,
            this.hideArmour,
            this.hideHunger,

            this.healthDropdown,
            this.health,
            this.healthColour,
            this.healthBar,
            this.healthBarColour,

            this.manaDropdown,
            this.mana,
            this.manaColour,
            this.manaBar,
            this.manaBarColour,

            this.otherDropdown,
            this.defence,
            this.defenceColour,
            this.effectiveHealth,
            this.speed,
            this.overflowMana,
            this.stacks,
            this.salvation
        )
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.isCanceled) return
        if (!inSkyblock) return

        event.isCanceled = when (event.type) {
            RenderGameOverlayEvent.ElementType.HEALTH -> hideHealth.enabled
            RenderGameOverlayEvent.ElementType.ARMOR -> hideArmour.enabled
            RenderGameOverlayEvent.ElementType.FOOD -> hideHunger.enabled
            else -> return
        }
    }

    fun modifyActionBar(text: String): String {
        var toReturn = text

        if (health.enabled) {
            toReturn = toReturn.replace(HP_REGEX, "")
        }
        if (mana.enabled) {
            toReturn = toReturn.replace(MANA_REGEX, "")
        }
        if (overflowMana.enabled) {
            toReturn = toReturn.replace(OVERFLOW_REGEX, "")
        }
        if (defence.enabled) {
            toReturn = toReturn.replace("[\\\\d|,]+§a❈ Defense".toRegex(), "") // temp
        }
        if (stacks.enabled) {
            toReturn = toReturn.replace(STACKS_REGEX, "")
        }
        if (salvation.enabled) {
            toReturn = toReturn.replace(SALVATION_REGEX, "")
        }

        return toReturn
    }

    @RegisterHudElement
    object HealthHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("1,200/10,900"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("1,200/10,900", 0.0, 0.0, healthColour.value.rgb) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !health.enabled) return
            FontUtil.drawStringWithShadow("${SkyblockPlayer.health.commas()}/${SkyblockPlayer.maxHealth.commas()}", 0.0, 0.0, healthColour.value.rgb)
        }
    }

    @RegisterHudElement
    object HealthBarHud : HudElement(
        this, 0, 0,
        barWidth.toInt(), barHeight.toInt() + 4,
        preview = {
            drawRoundedRect(0.0, 0.0, barWidth, barHeight, 5.0, Color(ColorUtil.bgColor))
            drawRoundedRect(0.0, 0.0, 50.0, barHeight, 5.0, healthBarColour.value)
            drawOutlinedRectBorder(0.0, 0.0, barWidth, barHeight, 5.0, 1.0, Color(208, 208, 208))
        }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !healthBar.enabled) return
            val fillWidth = SkyblockPlayer.health.toFloat() / SkyblockPlayer.maxHealth.toFloat() * barWidth
            drawRoundedRect(0.0, 0.0, barWidth, barHeight, 5.0, Color(ColorUtil.bgColor))
            drawRoundedRect(0.0, 0.0, fillWidth, barHeight, 5.0, healthBarColour.value)
            drawOutlinedRectBorder(0.0, 0.0, barWidth, barHeight, 5.0, 1.0, Color(208, 208, 208))
        }
    }

    @RegisterHudElement
    object ManaHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("1,300/10,900"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("1,300/10,900", 0.0, 0.0, manaColour.value.rgb) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !mana.enabled) return
            FontUtil.drawStringWithShadow("${SkyblockPlayer.mana.commas()}/${SkyblockPlayer.maxMana.commas()}", 0.0, 0.0, manaColour.value.rgb)
        }
    }

    @RegisterHudElement
    object ManaBarHud : HudElement(
        this, 0, 0,
        barWidth.toInt(), barHeight.toInt() + 4,
        preview = {
            drawRoundedRect(0.0, 0.0, barWidth, barHeight, 5.0, Color(ColorUtil.bgColor))
            drawRoundedRect(0.0, 0.0, 50.0, barHeight, 5.0, manaBarColour.value)
            drawOutlinedRectBorder(0.0, 0.0, barWidth, barHeight, 5.0, 1.0, Color(208, 208, 208))
        }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !manaBar.enabled) return
            val fillWidth = SkyblockPlayer.mana.toFloat() / SkyblockPlayer.maxMana.toFloat() * barWidth
            drawRoundedRect(0.0, 0.0, barWidth, barHeight, 5.0, Color(ColorUtil.bgColor))
            drawRoundedRect(0.0, 0.0, fillWidth, barHeight, 5.0, manaBarColour.value)
            drawOutlinedRectBorder(0.0, 0.0, barWidth, barHeight, 5.0, 1.0, Color(208, 208, 208))
        }
    }

    @RegisterHudElement
    object DefenceHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("10,000"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("10,000", 0.0, 0.0, defenceColour.value.rgb) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !defence.enabled) return
            FontUtil.drawStringWithShadow(SkyblockPlayer.defence.commas(), 0.0, 0.0, defenceColour.value.rgb)
        }
    }

    @RegisterHudElement
    object EffectiveHealthHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("54,879"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("54,879", 0.0, 0.0) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !effectiveHealth.enabled) return
            FontUtil.drawStringWithShadow(SkyblockPlayer.effectiveHealth.commas(), 0.0, 0.0)
        }
    }

    @RegisterHudElement
    object SpeedHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("500✦"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("500✦", 0.0, 0.0) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !speed.enabled) return
            FontUtil.drawStringWithShadow("${SkyblockPlayer.speed}✦", 0.0, 0.0)
        }
    }

    @RegisterHudElement
    object OverflowManaHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("600ʬ"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("600ʬ", 0.0, 0.0) }

    ) {
        override fun renderHud() {
            if (!inSkyblock || !overflowMana.enabled || SkyblockPlayer.overflowMana == 0) return
            FontUtil.drawStringWithShadow("${SkyblockPlayer.overflowMana.commas()}ʬ", 0.0, 0.0)
        }
    }

    @RegisterHudElement
    object StacksHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("10ᝐ"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("10ᝐ", 0.0, 0.0) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !stacks.enabled || SkyblockPlayer.stacks.isEmpty()) return
            FontUtil.drawStringWithShadow(SkyblockPlayer.stacks, 0.0, 0.0)
        }
    }

    @RegisterHudElement
    object SalvationHud : HudElement(
        this, 0, 0,
        mc.fontRendererObj.getStringWidth("T3!"),
        mc.fontRendererObj.FONT_HEIGHT + 2,
        preview = { FontUtil.drawStringWithShadow("T3!", 0.0, 0.0) }
    ) {
        override fun renderHud() {
            if (!inSkyblock || !overflowMana.enabled || SkyblockPlayer.salvation == 0) return
            FontUtil.drawStringWithShadow("T${SkyblockPlayer.salvation}!", 0.0, 0.0)
        }
    }

    private fun Number.commas(): String = NumberFormat.getInstance(Locale.US).format(this)
}