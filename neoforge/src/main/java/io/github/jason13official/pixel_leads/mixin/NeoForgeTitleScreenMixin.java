package io.github.jason13official.pixel_leads.mixin;

import io.github.jason13official.pixel_leads.Constants;
import io.github.jason13official.pixel_leads.platform.Services;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class NeoForgeTitleScreenMixin {

  @Inject(at = @At("HEAD"), method = "init()V")
  private void init(CallbackInfo info) {

    if (Services.PLATFORM.isDevelopmentEnvironment()) {
      Constants.LOG.info("This line is printed by an example mixin from NeoForge!");
    }
  }
}