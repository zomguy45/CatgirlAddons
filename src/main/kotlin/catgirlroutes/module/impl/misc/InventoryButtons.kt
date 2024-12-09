package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.display
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.ActionSetting
import catgirlroutes.ui.misc.InventoryButtonEditor

object InventoryButtons : Module(
    "Inventory buttons",
    Category.MISC
) {
    val editMode: ActionSetting = ActionSetting("Edit") { display = InventoryButtonEditor() }

    init {
        addSettings(this.editMode)
    }
}