package snownee.snow.world.gen.feature;

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
    public static final ConfiguredFeature<?, ?> CONFIGURED_FEATURE = FEATURE.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);

}
