package com.aelew.weavy.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public final class EntityLivingBaseMixin {

    @Shadow
    private int jumpTicks;

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    public void hookOnLivingUpdate(final CallbackInfo ci) {
        jumpTicks = 1;
    }

}