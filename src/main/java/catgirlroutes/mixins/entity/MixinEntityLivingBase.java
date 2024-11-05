package catgirlroutes.mixins.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityLivingBase.class})
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Shadow public abstract boolean isPotionActive(Potion potionIn);

    @Shadow public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Inject(method = {"getArmSwingAnimationEnd()I"}, at = @At("HEAD"), cancellable = true)
    public void adjustSwingLength(CallbackInfoReturnable<Integer> cir) {
    }
}