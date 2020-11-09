package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import snownee.snow.MixinConstants;

public class Connector implements IMixinConnector {

    @Override
    public void connect() {
        MixinConstants.logger.info("Invoking Mixin Connector");
        Mixins.addConfiguration("assets/snowrealmagic/snowrealmagic.mixins.json");
        MixinConstants.mixin = true;
    }

}
