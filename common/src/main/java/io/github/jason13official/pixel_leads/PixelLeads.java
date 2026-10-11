package io.github.jason13official.pixel_leads;

import net.minecraft.resources.ResourceLocation;

public class PixelLeads {

  public static void init() {
  }

  public static ResourceLocation identifier(final String path) {

    return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
  }
}