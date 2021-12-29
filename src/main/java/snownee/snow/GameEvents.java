package snownee.snow;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.tool.attribute.v1.ToolManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
import snownee.snow.block.SnowVariant;
import snownee.snow.block.entity.SnowBlockEntity;

public final class GameEvents {

	public static void init() {
		UseBlockCallback.EVENT.register(GameEvents::onItemUse);
		PlayerBlockBreakEvents.AFTER.register(GameEvents::onDestroyedByPlayer);
		ServerTickEvents.END_WORLD_TICK.register(GameEvents::onWorldTick);
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
		ItemStack held = player.getMainHandItem();
		if (!ToolManager.handleIsEffectiveOn(Blocks.SNOW.defaultBlockState(), held, player)) {
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
		} else {
			BlockState newState = ((SnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
			worldIn.setBlockAndUpdate(pos, newState);
			if (!player.isCreative() && player instanceof ServerPlayer) {
				if (newState.canOcclude())
					pos = pos.above();
				Block.popResource(worldIn, pos, new ItemStack(Items.SNOWBALL));
				held.hurtAndBreak(1, player, stack -> {
					stack.broadcastBreakEvent(InteractionHand.MAIN_HAND);
				});
			}
		}

		return InteractionResult.sidedSuccess(worldIn.isClientSide);
	}

	public static void onDestroyedByPlayer(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (player.isCreative() && blockEntity instanceof SnowBlockEntity) {
			BlockState newState = ((SnowBlockEntity) blockEntity).getState();
			world.setBlockAndUpdate(pos, newState);
		}
	}

	public static void onWorldTick(ServerLevel world) {
		if (SnowCommonConfig.placeSnowInBlock) {
			WorldTickHandler.tick(world);
		}
	}

}
