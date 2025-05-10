package catgirlroutes.module.impl.render

import catgirlroutes.CatgirlRoutes
import catgirlroutes.module.Category
import catgirlroutes.module.Module
import catgirlroutes.module.settings.impl.BooleanSetting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.Desktop
import java.net.URI
import java.net.URL
import javax.imageio.ImageIO

object CustomStartMenu : Module(
    name = "Start menu",
    category = Category.RENDER
) {

}

object CustomStartScreen : GuiScreen() {

    //Hardcoded zzz
    override fun initGui() {
        buttonList.add(CustomButton(0, 10, 10, 200, 20, "Singleplayer"))
        buttonList.add(CustomButton(1, 10, 35, 200, 20, "Multiplayer"))
        buttonList.add(CustomButton(2, 10, 60, 200, 20, "Options"))
        buttonList.add(CustomButton(3, 10, 85, 200, 20, "Hypixel"))
        buttonList.add(CustomButton(4, 10, height - 30, 200, 20, "Quit"))
        buttonList.add(CustomButton(5, width - 210,  height - 30, 200, 20, "Open Github"))
        buttonList.add(CustomButton(6, width - 210,  height - 55, 200, 20, "Random cat picture"))
        super.initGui()
    }

    override fun actionPerformed(button: GuiButton?) {
        when (button!!.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(mc.currentScreen))
            1 -> mc.displayGuiScreen(GuiMultiplayer(mc.currentScreen))
            2 -> mc.displayGuiScreen(GuiOptions(mc.currentScreen, mc.gameSettings))
            3 -> GuiConnecting(this, mc, ServerData("Hypixel", "mc.hypixel.net.:25565", false))
            4 -> mc.shutdown()
            5 -> Desktop.getDesktop().browse(URI("https://github.com/WompWatr/CatgirlAddons"))
            6 -> downloadCatImage { texture -> catTexture = texture }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        mc.textureManager.bindTexture(ResourceLocation(CatgirlRoutes.RESOURCE_DOMAIN, "gui/custom_background.png"))
        drawScaledCustomSizeModalRect(0, 0, 0f, 0f, 512, 512, width, height, 512f, 512f)

        catTexture?.let {
            drawRect(width - 210, 10, width - 10, 210, 0xFFEF89AF.toInt())
            mc.textureManager.bindTexture(it)
            drawModalRectWithCustomSizedTexture(width - 209, 11, 0f, 0f, 198, 198, 198f, 198f)
        }
        //drawStringWithShadow("Catgirl Addons on top!", 240f, 15f, 0xFF69B4, 4f)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private var catTexture: ResourceLocation? = null

    //chatgpt <3
    fun downloadCatImage(callback: (ResourceLocation?) -> Unit) {
        Thread {
            try {
                // Fetch the image on a background thread
                val url = URL("https://cataas.com/cat")
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                val inputStream = connection.getInputStream()
                val image = ImageIO.read(inputStream)

                if (image == null) {
                    println("Failed to load image")
                    callback(null)
                    return@Thread
                }

                // Now that the image is fetched, schedule the texture loading on the main thread
                Minecraft.getMinecraft().addScheduledTask {
                    try {
                        // Create the texture on the main thread with the OpenGL context
                        val texture = DynamicTexture(image)
                        val location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("cat", texture)
                        callback(location)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }



}

class CustomButton(
    id: Int,
    val xPos: Int,
    val yPos: Int,
    val customWidth: Int,
    val customHeight: Int,
    val label: String
) : GuiButton(id, xPos, yPos, customWidth, customHeight, label) {

    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        if (mc == null || !visible) return

        val hovered = mouseX in xPosition..(xPosition + width) &&
                mouseY in yPosition..(yPosition + height)

        val color = if (hovered) 0xFFFD2D79.toInt() else 0xFFEF89AF.toInt()

        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color)

        val textColor = if (hovered) 0xFFFFFF else 0xCCCCCC
        drawCenteredString(mc.fontRendererObj, label, xPosition + width / 2, yPosition + (height - 8) / 2, textColor)
    }
}




