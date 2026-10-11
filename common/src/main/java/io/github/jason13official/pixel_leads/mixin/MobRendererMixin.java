package io.github.jason13official.pixel_leads.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jason13official.pixel_leads.PixelLeash;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin {

  @Unique
  private static float pixel_leads$dx, pixel_leads$dy, pixel_leads$dz;
  @Unique
  private static int pixel_leads$startBlockLight, pixel_leads$endBlockLight, pixel_leads$startSkyLight, pixel_leads$endSkyLight;

  /// skip vanilla leash
  @Inject(method = "addVertexPair", at = @At("HEAD"), cancellable = true)
  private static void pixel_leads$addVertexPair(VertexConsumer buffer, Matrix4f pose, float startX, float startY, float startZ, int entityBlockLight, int holderBlockLight, int entitySkyLight, int holderSkyLight, float yOffset, float dy, float dx, float dz, int index, boolean reverse, CallbackInfo ci) {

    pixel_leads$dx = startX;
    pixel_leads$dy = startY;
    pixel_leads$dz = startZ;
    pixel_leads$startBlockLight = entityBlockLight;
    pixel_leads$endBlockLight = holderBlockLight;
    pixel_leads$startSkyLight = entitySkyLight;
    pixel_leads$endSkyLight = holderSkyLight;
    ci.cancel();
  }

  /// actually render the pixel leash
  @Inject(method = "renderLeash", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
  private void pixel_leads$renderLeash(Mob entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, Entity leashHolder, CallbackInfo ci) {

    PixelLeash.render(bufferSource.getBuffer(RenderType.textBackground()), poseStack.last().pose(), pixel_leads$dx, pixel_leads$dy, pixel_leads$dz, true, pixel_leads$startBlockLight, pixel_leads$endBlockLight, pixel_leads$startSkyLight, pixel_leads$endSkyLight);
  }
}
