package io.github.jason13official.pixel_leads;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class PixelLeadsForge {

  public PixelLeadsForge(final FMLJavaModLoadingContext context) {
  }

  @Deprecated
  @SuppressWarnings("all")
  public PixelLeadsForge() {
    this(FMLJavaModLoadingContext.get());
  }
}
