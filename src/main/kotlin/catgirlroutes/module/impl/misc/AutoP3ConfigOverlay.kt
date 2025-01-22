package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.commands.impl.RingManager.loadRings
import catgirlroutes.commands.impl.RingManager.saveRings
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.ui.misc.configoverlay.ConfigOverlay
import catgirlroutes.ui.misc.searchoverlay.BazaarSearchOverlay

object AutoP3ConfigOverlay: Module(
    name = "P3 Config",
    category = Category.DUNGEON
) {
    override fun onEnable() {
        saveRings()
        loadRings()
        display = ConfigOverlay()
        toggle()
        super.onEnable()
    }
}