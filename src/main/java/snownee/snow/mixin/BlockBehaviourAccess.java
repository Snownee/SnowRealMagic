package snownee.snow.mixin;

import net.minecraft.world.level.block.SoundType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccess {
	@Invoker("entityInside")
	void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity);

	@Invoker("getSoundType")
	SoundType getSoundType(BlockState blockState);
}
