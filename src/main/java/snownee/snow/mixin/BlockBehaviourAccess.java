package snownee.snow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccess {
	@Accessor
	BlockBehaviour.Properties getProperties();

	@Invoker
	void callEntityInside(BlockState state, Level level, BlockPos pos, Entity entity);

	@Invoker
	SoundType callGetSoundType(BlockState state);
}
