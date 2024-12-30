package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Module
import catgirlroutes.module.settings.RegisterHudElement
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.ui.hud.HudElement
import catgirlroutes.utils.dungeon.DungeonUtils.getMageCooldownMultiplier
import catgirlroutes.utils.dungeon.DungeonUtils.inDungeons
import catgirlroutes.utils.render.HUDRenderUtils.renderRect
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color

object InvincibilityTimer : Module(
    name = "Invincibility Timer"
) {
    val cataLevel = NumberSetting("Catacombs level", 0.0, 0.0, 50.0, 1.0)
    val dungeonOnly = BooleanSetting("Dungeons only", true)

    init {
        this.addSettings(cataLevel, dungeonOnly)
    }
    var spiritTicks = 0
    var bonzoTicks = 0
    var phoenixTicks = 0
    var phoenix = false

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val msg = event.message.unformattedText
        when {
            msg.contains("Second Wind Activated! Your Spirit Mask saved your life!") -> spiritTicks = (600 * getMageCooldownMultiplier()).toInt()
            msg.contains("Bonzo's Mask saved your life!") -> bonzoTicks = 7200 - cataLevel.value.toInt() * 72
            msg.contains("Your Phoenix Pet saved you from certain death!") -> phoenixTicks = (1200)
            msg.contains("You summoned your") -> phoenix = if (msg.contains("Phoenix")) true else false
            msg.contains("Autopet equipped your") -> phoenix = if (msg.contains("Phoenix")) true else false
            msg.contains("You despawned your Phoenix!") -> phoenix = false
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        spiritTicks -= 1
        phoenixTicks -= 1
        bonzoTicks -= 1
    }

    @RegisterHudElement
    object TimerHud : HudElement(
        this,
        12,
        12
    ) {
        override fun renderHud() {
            if (dungeonOnly.value && !inDungeons) return
            val name = mc.thePlayer?.inventory?.armorInventory?.get(3)?.displayName
            val offset = if (name?.contains("Bonzo") == true) 10.0 else if (name?.contains("Spirit") == true) 20.0 else 10000.0 //schizo way to not render indictator on screen
            val bonzoReady = if (bonzoTicks <= 0) "Bonzo: ${if (phoenix && offset == 10.0 && phoenixTicks < 0.0) "§c" else "§a"}✔" else "Bonzo: §c${(bonzoTicks / 20.0)}"
            val spiritReady = if (spiritTicks <= 0) "Spirit: ${if (phoenix && offset == 20.0 && phoenixTicks < 0.0) "§c" else "§a"}✔" else "Spirit: §c${(spiritTicks / 20.0)}"
            val phoenixReady = if (phoenixTicks <= 0) "Phoenix: ${if (phoenix && ((offset == 20.0 && spiritTicks < 0.0) || (offset == 10.0 && bonzoTicks < 0.0))) "§c" else "§a"}✔" else "Phoenix: §c${(phoenixTicks / 20.0)}"
            mc.fontRendererObj.drawStringWithShadow(bonzoReady, 0.0F, 10.0f, java.awt.Color.PINK.rgb)
            mc.fontRendererObj.drawStringWithShadow(spiritReady, 0.0F, 20.0f, java.awt.Color.PINK.rgb)
            mc.fontRendererObj.drawStringWithShadow(phoenixReady, 0.0F, 30.0f, java.awt.Color.PINK.rgb)
            renderRect(-6.0, offset + 2.0, 3.0, 3.0, Color.PINK)
            if (phoenix) renderRect(-6.0, 32.0, 3.0, 3.0, Color.PINK)
        }
    }
}