package snownee.snow;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;

@KiwiConfig
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
    public static boolean snowMeltsInWarmBiomes = false;
    public static boolean snowReduceFallDamage = true;
    public static boolean replaceWorldFeature = true;
    public static boolean sustainGrassIfLayerMoreThanOne = true;
    @Comment("If you want to uninstall this mod, you probably want to make snow-covered blocks back to normal.")
    public static boolean retainOriginalBlocks = false;

}
