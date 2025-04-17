package catgirlroutes.ui.clickgui.util

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.ui.Screen.Companion.CLICK_GUI_SCALE
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse

object MouseUtils {
    // prob should move somewhere else
    val sr get() = ScaledResolution(mc)
    val scale get() = CLICK_GUI_SCALE / sr.scaleFactor

    val mx get() = Mouse.getX()
    val my get() = mc.displayHeight - Mouse.getY()

    val mouseX get() = (mx / (scale * sr.scaleFactor)).toInt()
    val mouseY get() = ((my / sr.scaleFactor) / scale).toInt()
}