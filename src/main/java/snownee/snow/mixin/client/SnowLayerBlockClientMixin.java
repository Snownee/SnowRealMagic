package snownee.snow.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.client.SnowClientConfig;

@Mixin(SnowLayerBlock.class)
public class SnowLayerBlockClientMixin extends Block {

	public SnowLayerBlockClientMixin(Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!SnowClientConfig.particleThroughLeaves || random.nextInt(32) > 0) {
			return;
		}
		Entity entity = Minecraft.getInstance().getCameraEntity();
		if (entity != null && entity.blockPosition().distSqr(pos) > 256) {
			return;
		}
		BlockState stateDown = level.getBlockState(pos.below());
		if (stateDown.is(BlockTags.LEAVES)) {
			double d0 = pos.getX() + random.nextDouble();
			double d1 = pos.getY() - 0.05D;
			double d2 = pos.getZ() + random.nextDouble();
			level.addParticle(ParticleTypes.SNOWFLAKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

}