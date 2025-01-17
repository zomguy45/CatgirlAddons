package catgirlroutes.ui.misc

import catgirlroutes.module.impl.dungeons.LeapOrganiser
import catgirlroutes.CatgirlRoutes.Companion.mc as mainMc
import catgirlroutes.ui.clickgui.util.FontUtil
import catgirlroutes.ui.misc.elements.impl.MiscElementButton
import catgirlroutes.utils.ChatUtils.commandAny
import catgirlroutes.utils.ChatUtils.debugMessage
import catgirlroutes.utils.ChatUtils.modMessage
import catgirlroutes.utils.render.HUDRenderUtils.drawRoundedRect
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.abs

/**
 * WIP todo: party utils, update party button
 */
class LeapOrganiser : GuiScreen() {

    private val partyMembers: ArrayList<String> = arrayListOf("player1", "player2", "player3", "player4")
    private val boxes: ArrayList<Box> = ArrayList()
    private val slots: ArrayList<Slot> = ArrayList()

    private data class Box(var name: String, var x: Int, var y: Int)
    private data class Slot(var name: String, val x: Int, val y: Int, val colour: Color)

    private var selectedBox: Box? = null

    private var offsetX = 0
    private var offsetY = 0

    private val boxWidth = 160
    private val boxHeight = 80

    private val sr = ScaledResolution(mainMc)
    private val updateButton = MiscElementButton("Update Party", sr.scaledWidth_double / 2.0 - 40.0, sr.scaledHeight_double / 2.0 + 120) {
        debugMessage("Test button")
    }

    override fun initGui() {
        if (boxes.isNotEmpty()) return

        val centreX: Int = width / 2
        val centreY: Int = height / 2
        val spacing = 20

        slots.apply {
            clear()
            add(Slot("Player 1\n${LeapOrganiser.player1Note.text}", centreX - boxWidth - spacing, centreY - boxHeight - spacing, Color(255, 170, 21)))
            add(Slot("Player 2\n${LeapOrganiser.player2Note.text}", centreX + spacing, centreY - boxHeight - spacing, Color(170, 0, 0)))
            add(Slot("Player 3\n${LeapOrganiser.player3Note.text}", centreX - boxWidth - spacing, centreY + spacing, Color(85, 255, 255)))
            add(Slot("Player 4\n${LeapOrganiser.player4Note.text}", centreX + spacing, centreY + spacing, Color(0, 170, 0)))
        }

        boxes.apply {
            clear()
            partyMembers.forEachIndexed { i, member ->
                if (member != "_") add(Box(member, slots[i].x, slots[i].y))
            }
        }

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        for ((name, x, y, colour) in slots) {
            drawRoundedRect(x.toDouble(), y.toDouble(), boxWidth.toDouble(), boxHeight.toDouble(), 10.0, colour)
            name.split("\n").forEachIndexed { i, line ->
                FontUtil.drawString(
                    line,
                    x + boxWidth / 2 - mc.fontRendererObj.getStringWidth(line) / 2,
                    y + boxHeight / 2 - mc.fontRendererObj.FONT_HEIGHT * (1 - i),
                    -0x1000000
                )
            }
        }

        boxes.forEach { (name, x, y) ->
            drawRoundedRect(x.toDouble(), y.toDouble(), boxWidth.toDouble(), boxHeight.toDouble(), 10.0, Color(204, 204, 204, 150))
            FontUtil.drawString(name, x + 15, y + 15, -0x1000000)
        }

        mouseDrag(mouseX, mouseY)
        updateButton.isHovered(mouseX, mouseY)
        updateButton.render()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun mouseDrag(mouseX: Int, mouseY: Int) {
        selectedBox?.apply {
            x = mouseX - offsetX
            y = mouseY - offsetY
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        updateButton.mouseClicked(mouseX, mouseY, mouseButton)

        boxes.firstOrNull { isMouseOverBox(it, mouseX, mouseY) }?.let { box ->
            selectedBox = box
            offsetX = mouseX - box.x
            offsetY = mouseY - box.y
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state != 0 || selectedBox == null) return

        slots.firstOrNull { isWithinSnapRange(selectedBox!!, it) }?.let { slot ->
            getBoxInSlot(slot)?.takeIf { it !== selectedBox }?.let { box ->
                getEmptySlot()?.apply { box.x = x; box.y = y }
            }
            selectedBox!!.apply { x = slot.x; y = slot.y }
        }

        selectedBox = null
    }

    override fun onGuiClosed() {
        val order = slots.joinToString(" ") { getBoxInSlot(it)?.name ?: "_" }

        modMessage("Setting ${LeapOrganiser.leapMenu.selected} leap order: $order")
        LeapOrganiser.leapOrder.text = order
        when (LeapOrganiser.leapMenu.selected) {
            "SA" -> commandAny("/sa leap $order")
            "Odin" -> commandAny("/od leap $order")
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    private fun isMouseOverBox(box: Box, mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= box.x && mouseX <= box.x + boxWidth && mouseY >= box.y && mouseY <= box.y + boxHeight
    }

    private fun isWithinSnapRange(box: Box, slot: Slot): Boolean {
        val snapRange = 40
        val centreX = box.x + boxWidth / 2
        val centreY = box.y + boxHeight / 2

        return abs(centreX - (slot.x + boxWidth / 2)) <= snapRange + snapRange / 2 &&
                abs(centreY - (slot.y + boxHeight / 2)) <= snapRange
    }

    private fun getBoxInSlot(slot: Slot): Box? {
        return boxes.firstOrNull { it.x == slot.x && it.y == slot.y }
    }

    private fun getEmptySlot(): Slot? {
        return slots.firstOrNull { slot -> boxes.none { it.x == slot.x && it.y == slot.y } }
    }
}