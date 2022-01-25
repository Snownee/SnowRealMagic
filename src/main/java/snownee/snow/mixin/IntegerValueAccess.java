package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;

@Mixin(IntegerValue.class)
public interface IntegerValueAccess {

	@Invoker
	static GameRules.Type<GameRules.IntegerValue> callCreate(int p_46313_) {
		throw new IllegalStateException();
	}

}
