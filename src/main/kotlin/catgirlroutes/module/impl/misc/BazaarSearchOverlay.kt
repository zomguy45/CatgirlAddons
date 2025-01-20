package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.ui.misc.searchoverlay.BazaarSearchOverlay

object BazaarSearchOverlay : Module(
    "Bazaar Search",
    Category.MISC,
    tag = TagType.WHIP
) {
    override fun onEnable() {
        display = BazaarSearchOverlay()
        toggle()
        super.onEnable()
    }
}