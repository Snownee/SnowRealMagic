package snownee.snow.world.gen.feature;

import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;

@KiwiModule("world")
public class WorldModule extends AbstractModule {

    @Name("minecraft:freeze_top_layer")
    public static final ModIceAndSnowFeature FEATURE = new ModIceAndSnowFeature(NoFeatureConfig.field_236558_a_);
    // field_243794_T
    public static final ConfiguredFeature<?, ?> CONFIGURED_FEATURE = Registry.register(WorldGenRegistries.field_243653_e, "freeze_top_layer", FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));

}
