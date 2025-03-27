package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;

public final class GameEvents {

	public static InteractionResult onItemUse(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hitResult.getBlockPos();
		BlockState blockState = level.getBlockState(pos);
		if (!(blockState.getBlock() instanceof SnowVariant snowVariant)) {
			return InteractionResult.PASS;
		}
		ItemStack held = player.getMainHandItem();
		if (held.is(Items.DEBUG_STICK) || held.is(Items.SNOW)) {
			return InteractionResult.PASS;
		} else if (player.hasCorrectToolForDrops(Blocks.SNOW.defaultBlockState())) {
			if (playerCollectSnowball(level, pos, blockState, snowVariant) && !player.isCreative()) {
				Block.popResource(level, pos, new ItemStack(Items.SNOWBALL));
				held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else if (player.isSecondaryUseActive() && SnowCommonConfig.sneakSnowball) {
			if (playerCollectSnowball(level, pos, blockState, snowVariant)) {
				ItemStack snowball = new ItemStack(Items.SNOWBALL);
				if (!player.isCreative() || !player.getInventory().contains(snowball)) {
					if (!player.addItem(snowball)) {
						player.drop(snowball, false);
					}
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		} else if (!SnowCommonConfig.restoreOriginalBlocks && snowVariant.srm$canRenderOverlay(blockState) &&
				!player.isSecondaryUseActive() && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
			if (snowVariant.srm$renderLayerOffset(blockState) == 0) {
				BlockState stateBelow = level.getBlockState(pos.below());
				if (stateBelow.is(BlockTags.SNOW) || stateBelow.hasProperty(BlockStateProperties.SNOWY)) {
					return InteractionResult.PASS;
				}
			}
			if (blockState.is(Blocks.SNOW)) {
				level.setBlock(
						pos,
						Hooks.copyProperties(blockState, CoreModule.SNOW_BLOCK.defaultBlockState()),
						Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
			}
			if (!(level.getBlockEntity(pos) instanceof SnowBlockEntity be)) {
				return InteractionResult.PASS;
			}
			if (!blockState.is(Blocks.SNOW) && blockState.getBlock() instanceof SnowLayerBlock && be.getContainedState().isAir()) {
				level.setBlock(
						pos,
						Hooks.copyProperties(blockState, Blocks.SNOW.defaultBlockState()),
						Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
			} else {
				be.options.renderOverlay = !be.options.renderOverlay;
				be.refresh();
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
		BlockState newState = snowVariant.srm$decreaseLayer(state, level, pos, true);
		level.setBlockAndUpdate(pos, newState);
		int layers = snowVariant.srm$layers(state, level, pos);
		BlockState snowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, Math.max(layers, 1));
		level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(snowState));
		return layers > 0 || !state.hasProperty(SnowVariant.OPTIONAL_LAYERS);
	}

	public static boolean onDestroyedByPlayer(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if ((player == null || player.isCreative()) && blockEntity instanceof SnowBlockEntity) {
			BlockState newState = ((SnowBlockEntity) blockEntity).getContainedState();
			world.setBlockAndUpdate(pos, newState);
			return false;
		}
		return true;
	}

}
