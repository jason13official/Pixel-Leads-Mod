package io.github.jason13official.examplemod;

import io.github.jason13official.examplemod.impl.common.registry.ModBlocks;
import io.github.jason13official.examplemod.impl.common.registry.ModEntities;
import io.github.jason13official.examplemod.impl.common.registry.ModItems;
import io.github.jason13official.examplemod.impl.common.registry.ModMenus;
import io.github.jason13official.examplemod.impl.common.registry.ModParticles;
import io.github.jason13official.examplemod.impl.common.registry.ModTabs;
import io.github.jason13official.examplemod.impl.common.registry.ModTiles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class ExampleModNeoForge {

  public static IEventBus EVENT_BUS;

  public ExampleModNeoForge(final IEventBus modEventBus) {

    EVENT_BUS = modEventBus;

    bind(Registries.BLOCK, ModBlocks::register);
    bind(Registries.ENTITY_TYPE, ModEntities::register);
    bind(Registries.ITEM, ModItems::register);
    bind(Registries.PARTICLE_TYPE, ModParticles::register);
    bind(Registries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(Registries.MENU, ModMenus::register);
    bind(Registries.CREATIVE_MODE_TAB, ModTabs::register);

    EVENT_BUS.addListener((Consumer<FMLCommonSetupEvent>) event -> ExampleMod.init());

    NeoForge.EVENT_BUS.addListener((Consumer<AddServerReloadListenersEvent>) event -> {
      event.addListener(ExampleMod.identifier(Constants.MOD_ID), new ResourceReloadListener());
    });

    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
      new ExampleModClientNeoForge(EVENT_BUS);
    }
  }

  public <T> void bind(ResourceKey<Registry<T>> registryKey, Consumer<BiConsumer<T, Identifier>> source) {

    EVENT_BUS.addListener((Consumer<RegisterEvent>) event -> {
      if (registryKey.equals(event.getRegistryKey())) {
        source.accept((t, rl) -> event.register(registryKey, rl, () -> t));
      }
    });
  }

  public static class ResourceReloadListener extends SimplePreparableReloadListener<Void> {

    @Override
    public String getName() {
      return ExampleMod.identifier(Constants.MOD_ID).toString();
    }

    @Override
    protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      // ModConfig.load(Services.PLATFORM.getConfigDirectory());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
      return null;
    }
  }
}