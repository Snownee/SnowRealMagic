package snownee.snow;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = SnowRealMagic.MODID)
@Mod.EventBusSubscriber(modid = SnowRealMagic.MODID)
public final class ModConfig
{
    private ModConfig()
    {
        throw new UnsupportedOperationException("No instance for you");
    }

    @SubscribeEvent
    public static void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(SnowRealMagic.MODID))
        {
            ConfigManager.sync(SnowRealMagic.MODID, Config.Type.INSTANCE);
        }
    }

    @Config.Name("PlaceSnowInBlock")
    @Config.RequiresMcRestart
    public static boolean placeSnowInBlock = true;

    @Config.Name("SnowGravity")
    public static boolean snowGravity = true;

    @Config.Name("SnowAlwaysReplaceable")
    public static boolean snowAlwaysReplaceable = true;

    @Config.Name("SnowAccumulationDuringSnowstorm")
    public static boolean snowAccumulationDuringSnowstorm = true;

    @Config.Name("SnowAccumulationDuringSnowfall")
    public static boolean snowAccumulationDuringSnowfall = false;

    @Config.Name("ThinnerBoundingBox")
    public static boolean thinnerBoundingBox = true;

    @Config.Name("ParticleThroughLeaves")
    public static boolean particleThroughLeaves = true;

    @Config.Name("SnowMakingIce")
    public static boolean snowMakingIce = true;
}
