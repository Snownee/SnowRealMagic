package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import snownee.snow.block.SnowVariant;

public final class GameEvents {

	public static InteractionResult onItemUse(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hitResult.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof SnowVariant snowVariant)) {
			return InteractionResult.PASS;
		}
		ItemStack held = player.getMainHandItem();
		if (held.is(Items.DEBUG_STICK) || held.is(Items.SNOW)) {
			return InteractionResult.PASS;
		} else if (player.hasCorrectToolForDrops(Blocks.SNOW.defaultBlockState())) {
			if (playerCollectSnowball(level, pos, state, snowVariant) && !player.isCreative()) {
				Block.popResource(level, pos, new ItemStack(Items.SNOWBALL));
				held.hurtAndBreak(1, player, stack -> stack.broadcastBreakEvent(InteractionHand.MAIN_HAND));
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else if (player.isSecondaryUseActive() && SnowCommonConfig.sneakSnowball) {
			if (playerCollectSnowball(level, pos, state, snowVariant)) {
				ItemStack snowball = new ItemStack(Items.SNOWBALL);
				if (!player.isCreative() || !player.getInventory().contains(snowball)) {
					if (!player.addItem(snowball)) {
						player.drop(snowball, false);
					}
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	/**
	 * @return should drop snowball
	 */
	private static boolean playerCollectSnowball(Level level, BlockPos pos, BlockState state, SnowVariant snowVariant) {
		if (level.isClientSide) {
			return false;
		}
		BlockState newState = snowVariant.decreaseLayer(state, level, pos, true);
		level.setBlockAndUpdate(pos, newState);
		int layers = snowVariant.layers(state, level, pos);
		BlockState snowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.max(layers, 1));
		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(snowState));
		return layers > 0 || !state.hasProperty(SnowVariant.OPTIONAL_LAYERS);
	}

	public static boolean onDestroyedByPlayer(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		return state.onDestroyedByPlayer(world, pos, player, false, state.getFluidState());
	}

}
