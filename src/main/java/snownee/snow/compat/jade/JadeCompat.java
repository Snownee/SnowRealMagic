package snownee.snow.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.kiwi.loader.Platform;
import snownee.snow.compat.diagonalfences.DiagonalFencesCompat;
import snownee.snow.compat.diagonalwalls.DiagonalWallsCompat;
import snownee.snow.util.CommonProxy;

@WailaPlugin
public class JadeCompat implements IWailaPlugin {
	@Override
	public void registerClient(IWailaClientRegistration registration) {
		CommonProxy.allSnowBlocks().forEach(registration::usePickedResult);
		if (Platform.isModLoaded("diagonalwalls")) {
			DiagonalWallsCompat.getBlockConversions().values().forEach(registration::usePickedResult);
		}
		if (Platform.isModLoaded("diagonalfences")) {
			DiagonalFencesCompat.getBlockConversions().values().forEach(registration::usePickedResult);
		}
	}
}