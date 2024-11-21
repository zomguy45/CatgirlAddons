package catgirlroutes.module.impl.dungeons

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.KeyBindSetting
import catgirlroutes.utils.ChatUtils
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.Utils.leftClick
import catgirlroutes.utils.Utils.swapFromName
import org.lwjgl.input.Keyboard

object StonkSwap: Module( // todo: Merge with GhostBlocks ?maybe?
    "Stonk Swap",
    category = Category.MISC,
    description = "A module that automatically makes a ghost block using a swap to pickaxe."
){

    private var keyBindTest: KeyBindSetting = KeyBindSetting("TEST", Keyboard.KEY_NONE).onPress {
        if (!this.enabled) return@onPress
        ChatUtils.modMessage("TEST")
    }

    private var running = false
    private var prevSlot = -1

    init {
        addSettings(keyBindTest)
    }

    override fun onKeyBind() {
        if (running) return; // todo: do something about it idk?!

        running = true;
        prevSlot = mc.thePlayer.inventory.currentItem;

        val swapResult = swapFromName("pickaxe")
        if (!swapResult) {
            running = false
            return
        }

        scheduleTask(1) { leftClick() }
        scheduleTask(2) {
            mc.thePlayer.inventory.currentItem = prevSlot
            running = false
        }
    }
}