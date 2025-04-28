package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.commands.commodore
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import catgirlroutes.module.settings.impl.ColorSetting
import catgirlroutes.utils.ChatUtils.createClickableText
import catgirlroutes.utils.ChatUtils.getPrefix
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.ConfigSystem
import catgirlroutes.utils.render.WorldRenderUtils.drawBoxByEntity
import catgirlroutes.utils.render.WorldRenderUtils.drawTracer
import com.github.stivais.commodore.utils.GreedyString
import com.google.gson.reflect.TypeToken
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.EntityInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.io.File
import kotlin.math.abs

object CustomHighlight : Module(
    name = "Custom Highlight",
    category = Category.RENDER
) {
    val hlColor = ColorSetting("Color", Color.PINK)
    val tracer = BooleanSetting("Tracer", false)

    init {
        this.addSettings(hlColor, tracer)
    }

    private val highlightFile = File("config/catgirlroutes/highlight.json")

    var highlightList: MutableList<Highlight> = loadHighlight()

    private fun loadHighlight(): MutableList<Highlight> {
        return ConfigSystem.loadConfig(highlightFile, object : TypeToken<MutableList<Highlight>>() {}.type) ?: mutableListOf()
    }

    private fun saveHighlight() {
        ConfigSystem.saveConfig(highlightFile, highlightList)
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val entities = mc.theWorld.loadedEntityList
        entities.forEach{e ->
                if (e == mc.thePlayer) return@forEach
                val highlightMatch = highlightList.find { e.name.contains(it.tag, true)}

                if (highlightMatch != null) {
                    drawBoxByEntity(e, highlightMatch.color, e.width, e.height, phase = true)
                    if (tracer.value) {
                        drawTracer(e.positionVector, Color.PINK)
                    }
                }
        }
    }

    @SubscribeEvent
    fun onInteract(event: EntityInteractEvent) {
        if (!active) return
        mc.theWorld.loadedEntityList.forEach{e ->
            if (
                abs(e.posX - event.entity.posX) <= 3.0 &&
                abs(e.posY - event.entity.posY) <= 3.0 &&
                abs(e.posZ - event.entity.posZ) <= 3.0
            ) {
                val name = e.name
                if (e.name == mc.thePlayer.name) return@forEach
                createClickableText(getPrefix() + "Copy " + name, "Click to copy to clipboard!", "/hl schizophrenia ${name}")
                createClickableText(getPrefix() + "Add " + name, "Click to add to the list!", "/hl add ${name}")

            }
        }
    }

    private var active = false

    override fun onKeyBind() {
        active = !active
        modMessage("Tagging is now ${if (active) "active" else "inactive"}!")
    }

    val highlightCommands = commodore("hl") {
        literal("add").runs {
            tag: GreedyString ->
            highlightList.add(Highlight(tag.toString(), hlColor.value))
            saveHighlight()
            modMessage("Added ${tag} to the list!")
        }
        literal("clear").runs {
            highlightList.clear()
            saveHighlight()
            modMessage("List cleared!")
        }
        literal("remove").runs {
            tag: GreedyString ->
            highlightList.removeAll{h ->
                tag.toString() == (h.tag)
            }
            saveHighlight()
            modMessage("Removed ${tag} from the list!")
        }
        literal("list").runs {
            highlightList.forEach{h ->
                modMessage(h.tag)
            }
        }
        literal("schizophrenia").runs {
            text: GreedyString ->
            GuiScreen.setClipboardString(text.toString())
        }
    }
}

data class Highlight(val tag: String, val color: Color)