package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.utils.ClientListener.scheduleTask
import catgirlroutes.utils.Utils.leftClick
import catgirlroutes.utils.Utils.swapFromName

object StonkSwap: Module( // todo: Merge with GhostBlocks ?maybe?
    "Stonk Swap",
    category = Category.MISC,
    description = "A module that automatically makes a ghost block using a swap to pickaxe."
){

    private var running = false
    private var prevSlot = -1

    override fun onKeyBind() {
        if (running) return;

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