package io.github.jason13official.pixel_fishing_lines.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.jason13official.pixel_fishing_lines.PixelFishingLines;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

  /// capture the final level projection, CameraRenderState#projectionMatrix lacks view bobbing
  @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V", ordinal = 0))
  private void pixel_fishing_lines$captureProjection(DeltaTracker deltaTracker, CallbackInfo ci, @Local Matrix4f projectionMatrix) {

    PixelFishingLines.LEVEL_PROJECTION.set(projectionMatrix);
  }
}
