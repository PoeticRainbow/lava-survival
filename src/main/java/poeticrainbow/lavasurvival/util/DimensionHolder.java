package poeticrainbow.lavasurvival.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;

import java.util.Optional;

public interface DimensionHolder {
    Optional<DimensionOptions> lava_survival$getDimension(RegistryKey<DimensionOptions> dimension);
}
