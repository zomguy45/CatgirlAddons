package catgirlroutes.ui.clickguinew

import catgirlroutes.module.Category
import catgirlroutes.module.ModuleManager
import catgirlroutes.ui.animations.impl.EaseOutQuadAnimation
import catgirlroutes.ui.animations.impl.LinearAnimation
import catgirlroutes.ui.clickguinew.elements.ModuleButton
import catgirlroutes.utils.ChatUtils.debugMessage
import net.minecraft.client.renderer.GlStateManager

class Window( // todo: scroll shit
    val category: Category,
    val clickGui: ClickGUI
) {
    private val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    val x = this.clickGui.x + this.clickGui.categoryWidth + 10.0
    val y = this.clickGui.y + 25.0 + 5.0

    val width = this.clickGui.guiWidth - this.clickGui.categoryWidth - 20.0
    val height = this.clickGui.guiHeight - 25.0

    var length = 0.0

    private val selected: Boolean
        get() = this.clickGui.selectedWindow == this

    private var scrollTarget = 0.0
    private var scrollOffset = 0.0
    private var prevScrollPosition: Double? = null // todo: zzz
    private var whatToScroll: ModuleButton? = null

    private val scrollAnimation = LinearAnimation<Double>(200)
    private val addScrollAnimation = EaseOutQuadAnimation(500) // todo: impl

    init {
        for (module in ModuleManager.modules) {
            if (module.category != this.category) continue
            this.moduleButtons.add(ModuleButton(module, this))
        }
    }


    fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!this.selected) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)

        this.scrollOffset = this.scrollAnimation.get(this.scrollOffset, this.scrollTarget)

        var startYLeft = this.scrollOffset
        var startYRight = this.scrollOffset

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
                if (button.extendAnimation.isAnimating() || button.extraHeightAnimation.isAnimating()) {
                    if (button.extended) {
                        this.prevScrollPosition = this.prevScrollPosition ?: this.scrollOffset
                        val buttonBot = button.y + button.elementsHeight + button.height
                        val newScrollTarget = this.scrollOffset - if (button.elementsHeight + button.height < this.height) buttonBot - this.height + 15.0 else button.y

                        if (newScrollTarget < this.scrollTarget) {
                            this.scrollTarget = newScrollTarget
                            if (!this.scrollAnimation.isAnimating()) this.scrollAnimation.start()
                        }
                    }
//                    else if (this.prevScrollPosition != null) {
//                        this.scrollTarget = this.prevScrollPosition!!
//                        this.prevScrollPosition = null
//                        if (!this.scrollAnimation.isAnimating()) this.scrollAnimation.start()
//                    }
                }
            }
        }
        this.length = startYLeft.coerceAtLeast(startYRight)

        GlStateManager.popMatrix()
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (this.isHovered(mouseX, mouseY)) {
            this.moduleButtons.reversed().forEach {
                if (it.mouseClicked(mouseX, mouseY, mouseButton)) return true
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (this.selected) this.moduleButtons.reversed().forEach { it.mouseReleased(mouseX, mouseY, state) }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (this.selected) {
            this.moduleButtons.reversed().forEach {
                if (it.keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (this.selected) this.moduleButtons.reversed().forEach { it.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick) }
    }

    fun scroll(amount: Int, mouseX: Int, mouseY: Int): Boolean {
        if (isHovered(mouseX, mouseY)) {
            debugMessage(amount)
            this.scrollTarget = (this.scrollTarget + amount * SCROLL_DISTANCE).coerceIn(-this.length + this.scrollOffset + 72.0, 0.0)
            this.scrollAnimation.start(true)
            return true
        }
        return false
    }

    private fun isHovered(mouseX: Int, mouseY: Int): Boolean {
        if (!this.selected) return false
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height
    }

    companion object {
        private const val SCROLL_DISTANCE = 16
    }
}