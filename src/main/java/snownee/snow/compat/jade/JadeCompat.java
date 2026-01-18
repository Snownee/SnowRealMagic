package snownee.snow.compat.jade;

import java.util.Collection;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.kiwi.loader.Platform;
import snownee.snow.compat.diagonalfences.DiagonalFencesCompat;
import snownee.snow.compat.diagonalwalls.DiagonalWallsCompat;
import snownee.snow.util.CommonProxy;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
	@Override
	public void register(IWailaCommonRegistration registration) {
		registerPicks(registration, CommonProxy.allSnowBlocks());
		if (Platform.isModLoaded("diagonalwalls")) {
			registerPicks(registration, DiagonalWallsCompat.getBlockConversions().values());
		}
		if (Platform.isModLoaded("diagonalfences")) {
			registerPicks(registration, DiagonalFencesCompat.getBlockConversions().values());
		}
	}

	public static void registerPicks(IWailaCommonRegistration registration, Collection<Block> blocks) {
		blocks.forEach(block -> {
			registration.blockOperations().pick(block.defaultBlockState().typeHolder().unwrapKey().orElseThrow());
		});
	}
}