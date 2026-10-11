package io.github.jason13official.pixel_fishing_lines;

import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class PixelFishingLines {

  /// level projection as uploaded by the game renderer, including view bobbing and nausea distortion
  public static final Matrix4f LEVEL_PROJECTION = new Matrix4f();

  public static void init() {
  }

  public static Identifier identifier(final String path) {

    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
  }
}
