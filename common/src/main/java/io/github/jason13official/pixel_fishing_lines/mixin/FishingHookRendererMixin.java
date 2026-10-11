package io.github.jason13official.pixel_fishing_lines.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jason13official.pixel_fishing_lines.PixelFishingLines;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.projectile.FishingHook;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin extends EntityRenderer<FishingHook, FishingHookRenderState> {

  @Unique
  private static final int pixel_fishing_lines$PIXEL_SIZE = 2;
  @Unique
  private static final int pixel_fishing_lines$SEGMENTS = 32;
  @Unique
  private static final float pixel_fishing_lines$FULL_SIZE_DISTANCE = 4.0F;

  protected FishingHookRendererMixin(EntityRendererProvider.Context context) {
    super(context);
  }

  /// skip vanilla string
  @Inject(method = "stringVertex", at = @At("HEAD"), cancellable = true)
  private static void pixel_fishing_lines$stringVertex(float xa, float ya, float za, VertexConsumer stringBuffer, PoseStack.Pose stringPose, float aa, float nexta, float width, CallbackInfo ci) {

    ci.cancel();
  }

  @Unique
  private static void pixel_fishing_lines$cellQuad(VertexConsumer consumer, Matrix4f invMvp, int cx, int cy, float ndcZ, float cell, float width, float height) {

    pixel_fishing_lines$corner(consumer, invMvp, cx, cy, ndcZ, cell, width, height);
    pixel_fishing_lines$corner(consumer, invMvp, cx + 1, cy, ndcZ, cell, width, height);
    pixel_fishing_lines$corner(consumer, invMvp, cx + 1, cy + 1, ndcZ, cell, width, height);
    pixel_fishing_lines$corner(consumer, invMvp, cx, cy + 1, ndcZ, cell, width, height);
  }

  @Unique
  private static void pixel_fishing_lines$corner(VertexConsumer consumer, Matrix4f invMvp, int gx, int gy, float ndcZ, float cell, float width, float height) {

    float ndcX = gx * cell / width * 2.0F - 1.0F;
    float ndcY = gy * cell / height * 2.0F - 1.0F;
    Vector4f v = invMvp.transform(new Vector4f(ndcX, ndcY, ndcZ, 1.0F));

    // camera-relative world coords; the model-view matrix applies camera rotation at draw time
    consumer.addVertex(v.x / v.w, v.y / v.w, v.z / v.w).setColor(0xFF000000);
  }

  /// actually render the pixel string
  @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
  private void pixel_fishing_lines$submit(FishingHookRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {

    float dx = (float) state.lineOriginOffset.x;
    float dy = (float) state.lineOriginOffset.y;
    float dz = (float) state.lineOriginOffset.z;

    // camera-relative world -> clip space, using same matrices as the level renderer
    Matrix4f mvp = new Matrix4f(PixelFishingLines.LEVEL_PROJECTION).mul(camera.viewRotationMatrix);
    Matrix4f invMvp = new Matrix4f(mvp).invert();
    Matrix4f pose = poseStack.last().pose(); // translation from camera to the hook entity

    Window window = Minecraft.getInstance().getWindow();
    float width = window.getWidth();
    float height = window.getHeight();
    float baseCell = pixel_fishing_lines$PIXEL_SIZE * window.getGuiScale();

    // project the curve samples to (screenX, screenY, ndcZ, viewDepth) in framebuffer pixels; null when behind the camera
    float[][] pts = new float[pixel_fishing_lines$SEGMENTS + 1][];
    for (int i = 0; i <= pixel_fishing_lines$SEGMENTS; i++) {

      float t = (float) i / pixel_fishing_lines$SEGMENTS;

      Vector3f p = pose.transformPosition(dx * t, dy * (t * t + t) * 0.5F + 0.25F, dz * t, new Vector3f());
      Vector4f clip = mvp.transform(new Vector4f(p, 1.0F));

      if (clip.w <= 0.05F) {
        continue;
      }

      float sx = (clip.x / clip.w * 0.5F + 0.5F) * width;
      float sy = (clip.y / clip.w * 0.5F + 0.5F) * height;
      pts[i] = new float[]{sx, sy, clip.z / clip.w, clip.w};
    }

    // rasterize now, the geometry callback is deferred until the feature render pass
    List<float[]> cells = new ArrayList<>(); // (cx, cy, ndcZ, cell)
    int lastX = Integer.MIN_VALUE;
    int lastY = Integer.MIN_VALUE;
    float lastCell = 0.0F;
    for (int i = 0; i < pixel_fishing_lines$SEGMENTS; i++) {

      float[] a = pts[i];
      float[] b = pts[i + 1];

      if (a == null || b == null) {
        continue;
      }

      // pixel size shrinks with distance, snapped to whole framebuffer pixels (min 1)
      float depth = (a[3] + b[3]) * 0.5F;
      float scale = Math.min(1.0F, pixel_fishing_lines$FULL_SIZE_DISTANCE / depth);
      float cell = Math.max(1.0F, Math.round(baseCell * scale));

      // bresenham-style walk between the two samples, on a grid of this segment's pixel size

      int x0 = (int) Math.floor(a[0] / cell);
      int y0 = (int) Math.floor(a[1] / cell);
      int x1 = (int) Math.floor(b[0] / cell);
      int y1 = (int) Math.floor(b[1] / cell);

      int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
      for (int s = 0; s <= steps; s++) {

        float f = steps == 0 ? 0.0F : (float) s / steps;
        int cx = Math.round(x0 + (x1 - x0) * f);
        int cy = Math.round(y0 + (y1 - y0) * f);

        if (cx == lastX && cy == lastY && cell == lastCell) {
          continue;
        }

        lastX = cx;
        lastY = cy;
        lastCell = cell;

        float ndcZ = a[2] + (b[2] - a[2]) * f; // ndc depth is linear in screen space
        cells.add(new float[]{cx, cy, ndcZ, cell});
      }
    }

    if (cells.isEmpty()) {
      return;
    }

    submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (p, consumer) -> {
      for (float[] c : cells) {
        pixel_fishing_lines$cellQuad(consumer, invMvp, (int) c[0], (int) c[1], c[2], c[3], width, height);
      }
    });
  }
}
