package catgirlroutes.ui.misc

import catgirlroutes.CatgirlRoutes.Companion.RESOURCE_DOMAIN
import catgirlroutes.CatgirlRoutes.Companion.scope
import catgirlroutes.module.impl.misc.PhoenixAuth
import catgirlroutes.ui.Screen
import catgirlroutes.ui.clickgui.util.MouseUtils.mx
import catgirlroutes.ui.clickgui.util.MouseUtils.my
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.ui.misc.elements.impl.button
import catgirlroutes.utils.downloadImage
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import catgirlroutes.utils.render.HUDRenderUtils.drawTexturedRect
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.FMLClientHandler
import java.awt.Color
import java.awt.Desktop
import java.net.URI

object CustomMainMenu: Screen(false) { // todo add more shit

    private var buttons = listOf<MiscElementButton>()

    private var catTexture: ResourceLocation? = null

    override fun onInit() {
        buttons = mutableListOf(
            button(10, 10, "Singleplayer") { mc.displayGuiScreen(GuiSelectWorld(mc.currentScreen)) },
            button(10, 35, "Multiplayer") { mc.displayGuiScreen(GuiMultiplayer(mc.currentScreen)) },
            button(10, 60, "Options") { mc.displayGuiScreen(GuiOptions(mc.currentScreen, mc.gameSettings)) },
            button(10, 85, "Hypixel") { FMLClientHandler.instance().connectToServer(this, ServerData("Hypixel", "mc.hypixel.net:25565", false)) },
            button(10, this@CustomMainMenu.height - 30, "Quit") { mc.shutdown() },
            button(this@CustomMainMenu.width - 210, this@CustomMainMenu.height - 30, "Open Github") { Desktop.getDesktop().browse(URI("https://github.com/WompWatr/CatgirlAddons")) },
            button(this@CustomMainMenu.width - 210, this@CustomMainMenu.height - 55, "Discord Server") { Desktop.getDesktop().browse(URI("https://discord.gg/jK4AXeVK8u")) },
            button(this@CustomMainMenu.width - 210, this@CustomMainMenu.height - 80, "Random cat picture") { downloadCatImage { catTexture = it } }
        )
        if (PhoenixAuth.addToMainMenu) {
            (buttons as MutableList).add(4, button(10, 110, "Phoenix") {
                FMLClientHandler.instance().connectToServer(this, ServerData("Phoenix", "${PhoenixAuth.phoenixProxy}:25565", false))
            })
        }
    }

    override fun draw() {
        drawTexturedRect(ResourceLocation(RESOURCE_DOMAIN, "gui/custom_background.png"), 0.0, 0.0, sr.scaledWidth_double, sr.scaledHeight_double)
        catTexture?.let {
            drawRoundedRect(width - 210.0, 10.0, 200.0, 200.0, 3.0, Color(239, 137, 175))
            drawTexturedRect(it, width - 209.0, 11.0, 198.0, 198.0)
        }
        buttons.forEach { it.draw(mx, my) }
    }

    override fun onMouseClick(mouseButton: Int) {
        buttons.forEach { it.onMouseClick(mx, my, mouseButton) }
    }

    private fun downloadCatImage(callback: (ResourceLocation?) -> Unit) {
        scope.launch {
            val image = downloadImage("https://cataas.com/cat") ?: run {
                println("Failed to load image")
                callback(null)
                return@launch
            }

            mc.addScheduledTask {
                val texture = DynamicTexture(image)
                val location = mc.renderEngine.getDynamicTextureLocation("cat", texture)
                callback(location)
            }
            callback(null)
        }
    }
    private fun button(x: Int, y: Int, title: String, action: () -> Unit) = button {
        at(x, y)
        size(200, 20)
        text = title
        textShadow = true
        colour = Color(239, 137, 175)
        hoverColour = Color(253, 45, 121)
        outlineColour = colour
        outlineHoverColour = hoverColour
        onClick { action.invoke() }
    }
}