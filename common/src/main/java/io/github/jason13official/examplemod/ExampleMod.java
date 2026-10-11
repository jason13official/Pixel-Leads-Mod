package io.github.jason13official.examplemod;

import net.minecraft.resources.Identifier;

public class ExampleMod {

  public static void init() {

    // Sailing.register(Constants.MOD_ID, MonoLib.createFilename(Constants.MOD_ID, "26.1.2", "1.0.0"));
  }

  public static Identifier identifier(final String path) {
    return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
  }
}