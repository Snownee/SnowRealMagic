package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(FenceGateBlock.class)
public interface FenceGateBlockAccess {

	@Accessor
	WoodType getType();

	@Accessor
	@Mutable
	void setType(WoodType type);

}
