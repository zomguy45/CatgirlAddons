package catgirlroutes.module.impl.misc

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import net.minecraftforge.client.event.sound.PlaySoundSourceEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CatMode: Module( // todo: add cat model mode from skytils april fools!!!?!
    "Cat mode",
    category = Category.MISC,
    description = "MEOWMEOWMEOWMEOWMEOWMEOWMEOWMEOWMEOWMEOW"
){

    @SubscribeEvent
    fun onSound(event: PlaySoundSourceEvent) {
        if (event.name == "mob.cat.meow" || !this.enabled) return
        if (event.isCancelable) {
            event.isCanceled = true
        }
        if (mc.theWorld == null) return
        mc.theWorld.playSound(
            event.sound.xPosF.toDouble(),
            event.sound.yPosF.toDouble(),
            event.sound.zPosF.toDouble(),
            "mob.cat.meow",
            event.sound.volume,
            event.sound.pitch,
            false
        )
    }


    @JvmStatic
    fun replaceText(text: String?): String? {
        if (text == null || !this.enabled) return text

        val replacement = "meow"
        return text.split("\\s+".toRegex())
            .joinToString(" ") { replacement }
    }

}