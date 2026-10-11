package io.github.jason13official.examplemod;

import net.fabricmc.api.ClientModInitializer;

public class ExampleModClientFabric implements ClientModInitializer {

  @Override
  public void onInitializeClient() {

    ExampleModClient.init();
  }
}
