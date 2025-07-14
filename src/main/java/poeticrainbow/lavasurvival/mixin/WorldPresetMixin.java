package poeticrainbow.lavasurvival.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPreset;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import poeticrainbow.lavasurvival.util.DimensionHolder;

import java.util.Map;
import java.util.Optional;

@Debug(export = true)
@Mixin(WorldPreset.class)
public abstract class WorldPresetMixin implements DimensionHolder {
    @Shadow
    private final Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions;

    protected WorldPresetMixin(Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions) {
        this.dimensions = dimensions;
    }

    @Unique
    @Override
    public Optional<DimensionOptions> lava_survival$getDimension(RegistryKey<DimensionOptions> dimension) {
        return Optional.ofNullable(this.dimensions.get(dimension));
    }
}
