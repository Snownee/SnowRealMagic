package snownee.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.snow.block.SnowVariant;

@EventBusSubscriber
public final class GameEvents {

	@SubscribeEvent
	public static void onItemUse(PlayerInteractEvent.RightClickBlock event) {
		InteractionResult result = onItemUse(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
		if (result.consumesAction()) {
			event.setCanceled(true);
			event.setCancellationResult(result);
		}
	}

	public static InteractionResult onItemUse(Player player, Level worldIn, InteractionHand hand, BlockHitResult hitResult) {
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hitResult.getBlockPos();
		BlockState state = worldIn.getBlockState(pos);
		if (!(state.getBlock() instanceof SnowVariant)) {
			return InteractionResult.PASS;
		}
		if (player.hasCorrectToolForDrops(Blocks.SNOW.defaultBlockState())) {
			BlockState newState = ((SnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
			worldIn.setBlockAndUpdate(pos, newState);
			if (!player.isCreative() && player instanceof ServerPlayer) {
				if (newState.canOcclude())
					pos = pos.above();
				Block.popResource(worldIn, pos, new ItemStack(Items.SNOWBALL));
				ItemStack held = player.getMainHandItem();
				held.hurtAndBreak(1, player, stack -> {
					stack.broadcastBreakEvent(InteractionHand.MAIN_HAND);
				});
			}
		} else {
			if (!player.isSecondaryUseActive() || !SnowCommonConfig.sneakSnowball) {
				return InteractionResult.PASS;
			}
			BlockState newState = ((SnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
			worldIn.setBlockAndUpdate(pos, newState);
			ItemStack snowball = new ItemStack(Items.SNOWBALL);
			if (!player.isCreative() || !player.getInventory().contains(snowball)) {
				if (!player.addItem(snowball)) {
					player.drop(snowball, false);
				}
			}
		}

		return InteractionResult.sidedSuccess(worldIn.isClientSide);
	}

	public static boolean onDestroyedByPlayer(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		return state.onDestroyedByPlayer(world, pos, player, false, state.getFluidState());
	}

}
