package com.aelew.weavy.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerControllerMP.class)
public final class PlayerControllerMPMixin {

    @ModifyConstant(method = "clickBlock", constant = @Constant(intValue = 5))
    public int modifyBlockHitDelayConstant1(final int c) {
        return 0;
    }

    @ModifyConstant(method = "onPlayerDamageBlock", constant = @Constant(intValue = 5))
    public int modifyBlockHitDelayConstant2(final int c) {
        return 0;
    }

}
