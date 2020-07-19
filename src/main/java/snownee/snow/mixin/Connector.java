package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import snownee.snow.SnowRealMagic;

public class Connector implements IMixinConnector {

    @Override
    public void connect() {
        SnowRealMagic.logger.info("Invoking Mixin Connector");
        Mixins.addConfiguration("assets/snowrealmagic/snowrealmagic.mixins.json");
        SnowRealMagic.mixin = true;
    }

}
