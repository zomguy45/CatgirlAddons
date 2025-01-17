package catgirlroutes.ui.misc.inventorybuttons

import catgirlroutes.config.InventoryButtonsConfig
import catgirlroutes.ui.clickgui.util.ColorUtil
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.impl.MiscElementText
import catgirlroutes.utils.render.HUDRenderUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class InventoryButtonEditor : GuiScreen() {

    private val invWidth: Int = 176;
    private val invHeight: Int = 166;

    private var invX: Int = 0
    private var invY: Int = 0

    private var editingButton: InventoryButton? = null;

    private val editorWidth = 150.0
    private val editorHeight = 78.0
    private var editorX = 0
    private var editorY = 0

    private val commandTextField: MiscElementText = MiscElementText(editorWidth - 14, 16.0)
    private val iconTextField: MiscElementText = MiscElementText(editorWidth - 14, 16.0)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.drawDefaultBackground()

        invX = width / 2 - invWidth / 2;
        invY = height / 2 - invHeight / 2;

        GlStateManager.pushMatrix()
        GlStateManager.enableDepth()
        GlStateManager.color(1f, 1f, 1f, 1f)

        mc.textureManager.bindTexture(INVENTORY)

        HUDRenderUtils.drawTexturedRect(invX.toFloat(), invY.toFloat(), invWidth.toFloat(), invHeight.toFloat(), 0f, invWidth / 256f, 0f, invHeight / 256f, GL11.GL_NEAREST)

        InventoryButtonsConfig.allButtons.forEach { button ->
            val x: Int = invX + button.x
            val y: Int = invY + button.y

            // render button
            var c = InventoryButton.colour
            var bC = InventoryButton.borderColour
            if (editingButton == button) {
                c = c.brighter()
                bC = bC.brighter()
            }
            button.render(invX, invY, c, bC)

            if (!button.isActive()) {
                FontUtil.drawString("+", x + 6, y + 5)
            }
        }

        if (editingButton != null) {
            val x: Int = invX + editingButton!!.x
            val y: Int = invY + editingButton!!.y

            editorX = x + 8 - editorWidth.toInt() / 2
            editorY = y + 18 + 2

            // editor box
            HUDRenderUtils.drawRoundedBorderedRect(
                editorX.toDouble(),
                editorY.toDouble(),
                editorWidth,
                editorHeight,
                5.0, 1.0,
                Color(ColorUtil.bgColor), Color(ColorUtil.outlineColor)
            )


            FontUtil.drawString("Command", editorX + 7, editorY + 7, 0xffa0a0a0.toInt())
            commandTextField.setText(commandTextField.getText().replace("^ +", "")) // remove leading spaces
            if (commandTextField.getText().startsWith("/")) {
                commandTextField.prependText = ""
            } else {
                commandTextField.prependText = "§7/§r"
            }
            commandTextField.render(mouseX, mouseY, editorX + 7.0, editorY + 19.0)

            FontUtil.drawString("Icon", editorX + 7, editorY + 43, 0xffa0a0a0.toInt())
            iconTextField.setText(iconTextField.getText().replace("^ +", ""))
            iconTextField.render(mouseX, mouseY, editorX + 7.0, editorY + 55.0)
        }

        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        if (editingButton != null && isHoveringEditor(mouseX, mouseY)) {
            if (isHoveringText(mouseX, mouseY, 7, 12, commandTextField)) {
                commandTextField.mouseClicked(mouseX, mouseY, mouseButton)
                editingButton!!.command = commandTextField.getText()
                iconTextField.focus = false
                return
            }

            if (isHoveringText(mouseX, mouseY, 7, 55, iconTextField)) {
                iconTextField.mouseClicked(mouseX, mouseY, mouseButton)
                editingButton!!.icon = iconTextField.getText()
                commandTextField.focus = false
                return
            }
            return
        }


        InventoryButtonsConfig.allButtons.forEach { button ->
            if (button.isEquipment) return@forEach
            if (button.isHovered(mouseX - invX, mouseY - invY)) {
                if (editingButton == button && !isHoveringEditor(mouseX, mouseY)) {
                    editingButton = null
                } else {
                    editingButton = button
                    commandTextField.focus = true
                    commandTextField.setText(editingButton!!.command)
                    iconTextField.setText(editingButton!!.icon)
                    InventoryButtonsConfig.save()
                }
                return
            }
        }

        iconTextField.focus = false
        commandTextField.focus = false

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {

        if (editingButton != null) {
            if (commandTextField.focus) {
                commandTextField.keyTyped(typedChar, keyCode)
                editingButton!!.command = commandTextField.getText()
            }
            if (iconTextField.focus) {
                iconTextField.keyTyped(typedChar, keyCode)
                editingButton!!.icon = iconTextField.getText()
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun onGuiClosed() {
        InventoryButtonsConfig.save()
        super.onGuiClosed()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    private fun isHoveringText(mouseX: Int, mouseY: Int, xOff: Int, yOff: Int, textField: MiscElementText): Boolean {
        return mouseX >= editorX + xOff && mouseX <= editorX + xOff + textField.getElementWidth() &&
                mouseY >= editorY + yOff && mouseY <= editorY + yOff + textField.getElementHeight()
    }

    private fun isHoveringEditor(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= editorX && mouseX <= editorX + editorWidth
                && (mouseY >= editorY) and (mouseY <= editorY + editorHeight)
    }

    companion object {
        val INVENTORY: ResourceLocation = ResourceLocation("minecraft:textures/gui/container/inventory.png")
    }
}