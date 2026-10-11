package io.github.jason13official.examplemod.platform;

import io.github.jason13official.examplemod.Constants;
import io.github.jason13official.examplemod.platform.services.IPlatformHelper;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class Services {

  public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

  private static <T> T load(Class<T> clazz) {

    Constants.LOG.info("Loading service {}", clazz);

    // A merged multi-loader jar carries every platform's provider entry in
    // META-INF/services, so ServiceLoader can hand back a provider whose
    // backing classes aren't on this platform's classpath. Touch a real
    // platform call per candidate - the wrong one throws NoClassDefFoundError
    // here, and we skip to the next entry instead of crashing.
    for (T helper : ServiceLoader.load(clazz)) {
      try {
        boolean dev = helper instanceof IPlatformHelper platform && platform.isDevelopmentEnvironment();
        if (dev) {
          Constants.LOG.info("Loaded {} for service {}", helper, clazz);
        }
        return helper;
      } catch (NoClassDefFoundError | ServiceConfigurationError e) {
        Constants.LOG.debug("Skipping incompatible platform helper {}", helper.getClass().getName());
      }
    }

    throw new IllegalStateException("Failed to load service for " + clazz.getName());
  }
}