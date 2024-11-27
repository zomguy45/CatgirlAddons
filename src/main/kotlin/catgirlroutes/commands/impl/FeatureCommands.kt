package catgirlroutes.commands.impl

import catgirlroutes.commands.commodore
import catgirlroutes.module.impl.dungeons.LavaClip
import catgirlroutes.module.impl.player.PearlClip

val pearlClip = commodore("pearlclip") {
    runs { depth: Double? ->
        PearlClip.pearlClip(depth ?: 0.0)
    }
}

val lavaClip = commodore("lavaclip") {
    runs { depth: Double? ->
        LavaClip.lavaClipToggle(depth ?: 0.0);
    }
}




