package catgirlroutes.commands.impl

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.commodore
import catgirlroutes.module.impl.dungeons.LavaClip
import catgirlroutes.module.impl.dungeons.SecretAura
import catgirlroutes.module.impl.misc.AutoClicker.favItemsList
import catgirlroutes.module.impl.misc.InventoryButtons
import catgirlroutes.module.impl.player.BlockClip
import catgirlroutes.module.impl.player.PearlClip
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.skyblockID

val pearlClip = commodore("pearlclip") {
    runs { depth: Double? ->
        PearlClip.pearlClip(depth ?: 0.0)
    }
}

val lavaClip = commodore("lavaclip") {
    runs { depth: Double? ->
        LavaClip.lavaClipToggle(depth ?: 0.0)
    }
}

val blockClip = commodore("blockclip") {
    runs {  distance: Double? ->
        BlockClip.blockClip(distance ?: 1.0)
    }
}

val aura = commodore("cgaaura") {

    literal("help").runs {
        modMessage("""
            List of commands:
              §7/cgaaura enable §8: §renables Secret Aura
              §7/cgaaura disable §8: §rdisables Secret Aura
              §7/cgaaura clear §8: §rclears clicked blocks
        """.trimIndent())
    }

    literal("enable").runs {
        if (SecretAura.enabled) return@runs
        SecretAura.onKeyBind()
    }

    literal("disable").runs {
        if (!SecretAura.enabled) return@runs
        SecretAura.onKeyBind()
    }

    literal("clear").runs {
        SecretAura.clearBlocks()
        modMessage("Blocks cleared!")
    }
}

val inventoryButtons = commodore("cgabuttons") {
    runs {
        InventoryButtons.editMode.invoke()
    }
}

val autoClicker = commodore("cgaac") {
    literal("help").runs {
        modMessage("""
            List of commands:
              §7/cgaac help
              §7/cgaac add
              §7/cgaac remove
              §7/cgaac clear
        """.trimIndent())
    }

    literal("add").runs {
        val held = mc.thePlayer?.heldItem?.takeIf { it.skyblockID.isNotEmpty() } ?: return@runs modMessage("Not holding skyblock item")

        favItemsList.add(held.skyblockID)
        modMessage("Added ${held.displayName}!")
    }

    literal("remove").runs {
        val held = mc.thePlayer?.heldItem?.takeIf { it.skyblockID.isNotEmpty() } ?: return@runs modMessage("Not holding skyblock item")

        favItemsList.remove(held.skyblockID)
        modMessage("Removed ${held.displayName}!")
    }

    literal("clear") {
        favItemsList = mutableListOf()
        modMessage("Cleared!")
    }
}


