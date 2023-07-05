package com.mrcrayfish.vehicle.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getYRot()F"))
    private void onRenderLevel$getYRot(float delta, long time, PoseStack matrices, CallbackInfo ci)
    {
    //    CameraHandler.setupVehicleCamera(matrices);
    }
}
