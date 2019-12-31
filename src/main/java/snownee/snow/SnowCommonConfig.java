package snownee.snow;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.util.Util;

@EventBusSubscriber(bus = Bus.MOD)
public final class SnowCommonConfig {
    public static boolean placeSnowInBlock = true;
    public static boolean snowGravity = true;
    public static boolean snowAlwaysReplaceable = true;
    public static boolean snowAccumulationDuringSnowstorm = true;
    public static boolean snowAccumulationDuringSnowfall = false;
    public static boolean snowAccumulationOnSpecialBlocks = true;
    public static boolean thinnerBoundingBox = true;
    public static boolean snowMakingIce = true;
    public static boolean snowOnIce = false;
    public static boolean snowNeverMelt = false;
    public static boolean replaceWorldFeature = true;
    public static boolean forceNormalTESR = false;
    public static final Set<Block> invalidSupportingBlocks = Sets.newHashSet();

    private static BooleanValue placeSnowInBlockCfg;
    private static BooleanValue snowGravityCfg;
    private static BooleanValue snowAlwaysReplaceableCfg;
    private static BooleanValue snowAccumulationDuringSnowstormCfg;
    private static BooleanValue snowAccumulationDuringSnowfallCfg;
    private static BooleanValue snowAccumulationOnSpecialBlocksCfg;
    private static BooleanValue thinnerBoundingBoxCfg;
    private static BooleanValue snowMakingIceCfg;
    private static BooleanValue snowOnIceCfg;
    private static BooleanValue snowNeverMeltCfg;
    private static BooleanValue replaceWorldFeatureCfg;
    private static BooleanValue forceNormalTESRCfg;
    private static ConfigValue<List<? extends String>> invalidSupportingBlocksCfg;

    static final ForgeConfigSpec spec;

    static {
        final Pair<SnowCommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SnowCommonConfig::new);
        spec = specPair.getRight();
    }

    private SnowCommonConfig(ForgeConfigSpec.Builder builder) {
        placeSnowInBlockCfg = builder.define("placeSnowInBlock", placeSnowInBlock);
        snowGravityCfg = builder.define("snowGravity", snowGravity);
        snowAlwaysReplaceableCfg = builder.define("snowAlwaysReplaceable", snowAlwaysReplaceable);
        snowAccumulationDuringSnowstormCfg = builder.define("snowAccumulationDuringSnowstorm", snowAccumulationDuringSnowstorm);
        snowAccumulationDuringSnowfallCfg = builder.define("snowAccumulationDuringSnowfall", snowAccumulationDuringSnowfall);
        snowAccumulationOnSpecialBlocksCfg = builder.define("snowAccumulationOnSpecialBlocks", snowAccumulationOnSpecialBlocks);
        thinnerBoundingBoxCfg = builder.define("thinnerBoundingBox", thinnerBoundingBox);
        snowMakingIceCfg = builder.define("snowMakingIce", snowMakingIce);
        snowOnIceCfg = builder.define("snowOnIce", snowOnIce);
        snowNeverMeltCfg = builder.define("snowNeverMelt", snowNeverMelt);
        replaceWorldFeatureCfg = builder.define("replaceWorldFeature", replaceWorldFeature);
        forceNormalTESRCfg = builder.define("forceNormalTESR", forceNormalTESR);
        invalidSupportingBlocksCfg = builder.defineList("invalidSupportingBlocks", () -> Arrays.asList("ice", "packed_ice", "barrier"), null);
    }

    public static void refresh() {
        placeSnowInBlock = placeSnowInBlockCfg.get();
        snowGravity = snowGravityCfg.get();
        snowAlwaysReplaceable = snowAlwaysReplaceableCfg.get();
        snowAccumulationDuringSnowstorm = snowAccumulationDuringSnowstormCfg.get();
        snowAccumulationDuringSnowfall = snowAccumulationDuringSnowfallCfg.get();
        snowAccumulationOnSpecialBlocks = snowAccumulationOnSpecialBlocksCfg.get();
        thinnerBoundingBox = thinnerBoundingBoxCfg.get();
        snowMakingIce = snowMakingIceCfg.get();
        snowOnIce = snowOnIceCfg.get();
        snowNeverMelt = snowNeverMeltCfg.get();
        replaceWorldFeature = replaceWorldFeatureCfg.get();
        forceNormalTESR = forceNormalTESRCfg.get();
        invalidSupportingBlocks.clear();
        invalidSupportingBlocksCfg.get().forEach(id -> {
            ResourceLocation rl = Util.RL(id);
            if (rl != null && ForgeRegistries.BLOCKS.containsKey(rl)) {
                invalidSupportingBlocks.add(ForgeRegistries.BLOCKS.getValue(rl));
            }
        });
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
