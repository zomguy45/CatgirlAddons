package catgirlroutes.ui.clickguinew

import catgirlroutes.module.Category
import catgirlroutes.module.ModuleManager
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import net.minecraft.client.renderer.GlStateManager

class Window(
    val category: Category,
    val clickGui: ClickGUI
) {
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    val x = this.clickGui.x + this.clickGui.categoryWidth + 10.0
    val y = this.clickGui.y + 25.0 + 5.0

    val width = this.clickGui.guiWidth - this.clickGui.categoryWidth - 20.0
    val height = this.clickGui.guiHeight - 25.0

    private val selected: Boolean
        get() = this.clickGui.selectedWindow == this

    private var scrollOffset = 0

    init {
        for (module in ModuleManager.modules) {
            if (module.category != this.category) continue
            this.moduleButtons.add(ModuleButton(module, this))
        }
    }


    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!selected) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)

//        HUDRenderUtils.setUpScissorAbsolute(x, y + height - 5.0, x + width + 1, y + height)
//        HUDRenderUtils.drawRoundedRect(0.0, 0.0, 10.0, 10.0, 3.0, Color.WHITE)

        var startYLeft = 0.0
        var startYRight = 0.0

        this.moduleButtons.chunked(2).forEach { row ->
            row.forEachIndexed { i, button ->
                if (i == 0) {
                    button.x = 0.0
                    button.y = startYLeft
                    startYLeft += button.drawScreen(mouseX, mouseY, partialTicks)
                } else {
                    button.x = row[0].width + 5.0
                    button.y = startYRight
                    startYRight += button.drawScreen(mouseX, mouseY, partialTicks)
                }
            }
        }


//        HUDRenderUtils.endScissor()
        GlStateManager.popMatrix()
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {

    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return false
    }

    fun scroll(amount: Int, mouseX: Int, mouseY: Int): Boolean {
        return false
    }

    companion object {
        private const val SCROLL_DISTANCE = 11
    }
}