package catgirlroutes.commands.impl

import catgirlroutes.commands.commodore
import catgirlroutes.module.impl.misc.InventoryButtons

val inventoryButtonsCommands = commodore("cgabuttons") {
    runs {
        InventoryButtons.editMode.doAction()
    }
}