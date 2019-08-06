package snownee.snow;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public final class SnowClientConfig
{
    public static boolean particleThroughLeaves = true;

    private static BooleanValue particleThroughLeavesCfg;

    static final ForgeConfigSpec spec;

    static
    {
        final Pair<SnowClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SnowClientConfig::new);
        spec = specPair.getRight();
    }

    private SnowClientConfig(ForgeConfigSpec.Builder builder)
    {
        particleThroughLeavesCfg = builder.define("particleThroughLeaves", particleThroughLeaves);
    }

    public static void refresh()
    {
        particleThroughLeaves = particleThroughLeavesCfg.get();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading event)
    {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
