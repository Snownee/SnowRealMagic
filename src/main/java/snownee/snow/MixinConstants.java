package snownee.snow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MixinConstants {

    private MixinConstants() {}

    public static final String NAME = "Snow! Real Magic!";
    public static final Logger logger = LogManager.getLogger(NAME);
    public static boolean mixin;

}
