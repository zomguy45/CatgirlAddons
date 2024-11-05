package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.events.ClickEvent
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.Setting.Companion.withDependency
import catgirlroutes.module.settings.Setting.Companion.withInputTransform
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.NumberSetting
import catgirlroutes.utils.Utils
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random

object AutoClicker : Module(
    "Auto Clicker",
    category = Category.MISC,
    description = "A simple auto clicker for both left and right click. Activates when the corresponding key is being held down. "
) {
    private val leftClick = BooleanSetting("Left Click", true, description = "Toggles the auto clicker for left click.")
    private val maxCps: NumberSetting = NumberSetting("Max CPS", 12.0, 1.0, 20.0, 1.0, description = "Maximum cps for left click.")
        .withInputTransform { input, setting ->
            MathHelper.clamp_double(input, minCps.value, setting.max)
        }.withDependency { leftClick.enabled }

    private val minCps: NumberSetting = NumberSetting("Min CPS", 10.0, 1.0, 20.0, 1.0, description = "Minimum CPS for left click.")
        .withInputTransform { input, setting ->
            MathHelper.clamp_double(input, setting.min, maxCps.value)
        }.withDependency { leftClick.enabled }

    private val rightClick = BooleanSetting("Right Click", false, description = "Toggles the auto clicker for right click.")
    private val rightClickSleep = NumberSetting("RC Sleep", 100.0, 20.0, 200.0, 5.0, description = "Delay in between right clicks in milliseconds.")
        .withDependency { rightClick.enabled }

    private var nextLeftClick = System.currentTimeMillis()
    private var nextRightClick = System.currentTimeMillis()

    init {
        this.addSettings(
            leftClick,
            maxCps,
            minCps,
            rightClick,
            rightClickSleep,
        )
    }

    /**
     * Set the click delay.
     * Prevents double click on the initial click.
     */
    @SubscribeEvent
    fun onLeftClick(event: ClickEvent.LeftClickEvent) {
        if(leftClick.enabled) {
            val nowMillis = System.currentTimeMillis()
            nextLeftClick = nowMillis + Random.nextDouble(1000.0 / maxCps.value, 1000.0 / minCps.value).toInt()
        }
    }

    /**
     * Set the click delay.
     * Prevents double click on the initial click.
     */
    @SubscribeEvent
    fun onRightClick(event: ClickEvent.RightClickEvent) {
        if(rightClick.enabled) {
            val nowMillis = System.currentTimeMillis()
            val overshoot =  (nowMillis - nextRightClick).takeIf { it < 200 } ?: 0L
            nextRightClick = nowMillis + (rightClickSleep.value.toLong() - overshoot).coerceAtLeast(0L)
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val nowMillis = System.currentTimeMillis()

        if (mc.thePlayer != null && mc.currentScreen == null && !mc.thePlayer.isUsingItem) {

            if (leftClick.enabled && mc.gameSettings.keyBindAttack.isKeyDown && nowMillis >= nextLeftClick) {
                Utils.leftClick()
                // Set delay between clicks to a random interval based on CPS range
                val interval = (1000.0 / Random.nextDouble(minCps.value, maxCps.value)).toLong()
                nextLeftClick = nowMillis + interval
            }

            if (rightClick.enabled && mc.gameSettings.keyBindUseItem.isKeyDown && nowMillis >= nextRightClick) {
                Utils.rightClick()
                // Use right click delay for intervals
                nextRightClick = nowMillis + rightClickSleep.value.toLong()
            }
        }
    }
}