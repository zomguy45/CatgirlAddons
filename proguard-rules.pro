# Ignore missing references for JDK classes
-dontwarn java.**
-dontwarn javax.**

# Preserve your mod entry points
-keep public class catgirlroutes.CatgirlRoutes {
    public static void main(...);
}

# Keep Minecraft/Forge classes
-keep class net.minecraftforge.** { *; }
-keep class net.minecraft.** { *; }

# Obfuscate everything else
-repackageclasses ''
-dontshrink
-dontoptimize
