package snownee.snow;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(bus = Bus.MOD)
public final class SnowClientConfig {
    public static boolean particleThroughLeaves = true;
    public static boolean colorTint = true;

    private static BooleanValue particleThroughLeavesCfg;
    private static BooleanValue colorTintCfg;

    static final ForgeConfigSpec spec;

    static {
        final Pair<SnowClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SnowClientConfig::new);
        spec = specPair.getRight();
    }

    private SnowClientConfig(ForgeConfigSpec.Builder builder) {
        particleThroughLeavesCfg = builder.define("particleThroughLeaves", particleThroughLeaves);
        colorTintCfg = builder.define("colorTint", colorTint);
    }

    public static void refresh() {
        particleThroughLeaves = particleThroughLeavesCfg.get();
        colorTint = colorTintCfg.get();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
