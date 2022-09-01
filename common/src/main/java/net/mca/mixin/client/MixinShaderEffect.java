package net.mca.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.ShaderEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderEffect.class)
public class MixinShaderEffect {
    @Inject(method = "render(F)V", at = @At("TAIL"))
    public void render(float tickDelta, CallbackInfo ci) {
        GlStateManager.enableTexture();
    }
}
