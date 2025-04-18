package catgirlroutes.ui.clickgui.util

import catgirlroutes.CatgirlRoutes.Companion.mc
import catgirlroutes.utils.render.HUDRenderUtils.scale
import catgirlroutes.utils.render.HUDRenderUtils.sr
import org.lwjgl.input.Mouse

object MouseUtils {
    val mx get() = Mouse.getX()
    val my get() = mc.displayHeight - Mouse.getY()

    val mouseX get() = (mx / (scale * sr.scaleFactor)).toInt()
    val mouseY get() = ((my / sr.scaleFactor) / scale).toInt()
}