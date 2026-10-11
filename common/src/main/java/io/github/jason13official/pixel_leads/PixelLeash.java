package io.github.jason13official.pixel_leads;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/// rasterizes the vanilla leash curve onto a screen-aligned pixel grid
public final class PixelLeash {

  private static final int PIXEL_SIZE = 2;
  private static final int SEGMENTS = 24; // matches vanilla, one colour stripe per segment
  private static final float FULL_SIZE_DISTANCE = 4.0F;
  private static final float LEASH_WIDTH = 0.025F;

  private PixelLeash() {
  }

  /// draw a leash from the origin of `pose` to (dx, dy, dz); `slack` sags the curve like vanilla, otherwise it is straight
  public static void render(VertexConsumer consumer, Matrix4f pose, float dx, float dy, float dz, boolean slack, int startBlockLight, int endBlockLight, int startSkyLight, int endSkyLight) {

    // vertex space -> clip space, using the same matrices the batch is drawn with
    Matrix4f mvp = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix());
    Matrix4f invMvp = new Matrix4f(mvp).invert();

    Window window = Minecraft.getInstance().getWindow();
    float width = window.getWidth();
    float height = window.getHeight();
    float baseCell = (float) (PIXEL_SIZE * window.getGuiScale());

    // project the curve samples to (screenX, screenY, ndcZ, viewDepth) in framebuffer pixels; null when behind the camera
    float[][] pts = new float[SEGMENTS + 1][];
    for (int i = 0; i <= SEGMENTS; i++) {

      float t = (float) i / SEGMENTS;
      float y = !slack ? dy * t : dy > 0.0F ? dy * t * t : dy - dy * (1.0F - t) * (1.0F - t);

      // vanilla's ribbons span [y, y + width], follow their centre
      Vector3f p = pose.transformPosition(dx * t, y + LEASH_WIDTH * 0.5F, dz * t, new Vector3f());
      Vector4f clip = mvp.transform(new Vector4f(p, 1.0F));

      if (clip.w <= 0.05F) {
        continue;
      }

      float sx = (clip.x / clip.w * 0.5F + 0.5F) * width;
      float sy = (clip.y / clip.w * 0.5F + 0.5F) * height;
      pts[i] = new float[]{sx, sy, clip.z / clip.w, clip.w};
    }

    int lastX = Integer.MIN_VALUE;
    int lastY = Integer.MIN_VALUE;
    float lastCell = 0.0F;
    for (int i = 0; i < SEGMENTS; i++) {

      float[] a = pts[i];
      float[] b = pts[i + 1];

      if (a == null || b == null) {
        continue;
      }

      // vanilla colour and lighting for this segment
      float t = (float) i / SEGMENTS;
      int light = LightTexture.pack((int) Mth.lerp(t, (float) startBlockLight, (float) endBlockLight), (int) Mth.lerp(t, (float) startSkyLight, (float) endSkyLight));
      float shade = i % 2 == 0 ? 0.7F : 1.0F;
      int color = 0xFF000000 | (int) (0.5F * shade * 255.0F) << 16 | (int) (0.4F * shade * 255.0F) << 8 | (int) (0.3F * shade * 255.0F);

      // pixel size shrinks with distance, snapped to whole framebuffer pixels (min 1)
      float depth = (a[3] + b[3]) * 0.5F;
      float scale = Math.min(1.0F, FULL_SIZE_DISTANCE / depth);
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
        cellQuad(consumer, invMvp, cx, cy, ndcZ, cell, width, height, color, light);
      }
    }
  }

  private static void cellQuad(VertexConsumer consumer, Matrix4f invMvp, int cx, int cy, float ndcZ, float cell, float width, float height, int color, int light) {

    // counter-clockwise on screen, so the quad survives back-face culling
    corner(consumer, invMvp, cx, cy, ndcZ, cell, width, height, color, light);
    corner(consumer, invMvp, cx + 1, cy, ndcZ, cell, width, height, color, light);
    corner(consumer, invMvp, cx + 1, cy + 1, ndcZ, cell, width, height, color, light);
    corner(consumer, invMvp, cx, cy + 1, ndcZ, cell, width, height, color, light);
  }

  private static void corner(VertexConsumer consumer, Matrix4f invMvp, int gx, int gy, float ndcZ, float cell, float width, float height, int color, int light) {

    float ndcX = gx * cell / width * 2.0F - 1.0F;
    float ndcY = gy * cell / height * 2.0F - 1.0F;
    Vector4f v = invMvp.transform(new Vector4f(ndcX, ndcY, ndcZ, 1.0F));

    consumer.addVertex(v.x / v.w, v.y / v.w, v.z / v.w).setColor(color).setLight(light);
  }
}
