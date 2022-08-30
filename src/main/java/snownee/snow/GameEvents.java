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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemHandlerHelper;
import snownee.snow.block.SnowVariant;

@EventBusSubscriber
public final class GameEvents {

	@SubscribeEvent
	public static void onItemUse(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != InteractionHand.MAIN_HAND) {
			return;
		}
		Level worldIn = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = worldIn.getBlockState(pos);
		if (!(state.getBlock() instanceof SnowVariant)) {
			return;
		}
		Player player = event.getEntity();
		if (!ForgeHooks.isCorrectToolForDrops(CoreModule.BLOCK.defaultBlockState(), player)) {
			if (!player.isSecondaryUseActive() || !SnowCommonConfig.sneakSnowball) {
				return;
			}
			BlockState newState = ((SnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
			worldIn.setBlockAndUpdate(pos, newState);
			ItemStack snowball = new ItemStack(Items.SNOWBALL);
			if (!player.isCreative() || !player.getInventory().contains(snowball)) {
				ItemHandlerHelper.giveItemToPlayer(player, snowball);
			}
		} else {
			BlockState newState = ((SnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
			worldIn.setBlockAndUpdate(pos, newState);
			if (!player.isCreative() && player instanceof ServerPlayer) {
				if (newState.canOcclude())
					pos = pos.above();
				Block.popResource(worldIn, pos, new ItemStack(Items.SNOWBALL));
				player.getMainHandItem().hurtAndBreak(1, player, stack -> {
					stack.broadcastBreakEvent(InteractionHand.MAIN_HAND);
				});
			}
		}

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.sidedSuccess(worldIn.isClientSide));
	}

}
