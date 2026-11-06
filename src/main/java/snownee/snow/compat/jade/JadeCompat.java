package snownee.snow.compat.jade;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.kiwi.loader.Platform;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowRealMagic;
import snownee.snow.util.CommonProxy;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
	private static final Identifier SNOW_ACCUMULATION = SnowRealMagic.id("snow_accumulation");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registerPicks(registration, CommonProxy.allSnowBlocks());
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		if (!Platform.isProduction()) {
			registration.registerBlockComponent(new SnowAccumulationProvider(), Block.class);
		}
	}

	public static void registerPicks(IWailaCommonRegistration registration, Collection<Block> blocks) {
		blocks.forEach(block -> {
			registration.blockOperations().pick(block.defaultBlockState().typeHolder().unwrapKey().orElseThrow());
		});
	}

	private static final class SnowAccumulationProvider implements IBlockComponentProvider {
		@Override
		public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
			BlockState state = accessor.getBlockState();
			if (!SnowAccumulation.isSupported(state)) {
				return;
			}
			Level level = accessor.getLevel();
			BlockPos pos = accessor.getPosition();
			LevelChunk chunk = level.getChunkAt(pos);
			int snow = SnowAccumulation.get(chunk, pos);
			int max = SnowAccumulation.maxSnowState(level, pos);
			tooltip.add(Component.literal(max > 0 ? "Snow Layers: " + snow + "/" + max : "Snow Layers: " + snow));
		}

		@Override
		public Identifier getUid() {
			return SNOW_ACCUMULATION;
		}

		@Override
		public boolean isRequired() {
			return true;
		}
	}
}
