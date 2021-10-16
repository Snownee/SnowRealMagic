package snownee.snow;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.snow.block.SnowVariant;

@EventBusSubscriber
public final class GameEvents {

	@SubscribeEvent
	public static void onItemUse(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != Hand.MAIN_HAND) {
			return;
		}
		World worldIn = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = worldIn.getBlockState(pos);
		if (!(state.getBlock() instanceof SnowVariant)) {
			return;
		}
		PlayerEntity player = event.getPlayer();
		if (!ForgeHooks.canHarvestBlock(CoreModule.BLOCK.getDefaultState(), player, worldIn, pos)) {
			return;
		}
		BlockState newState = ((SnowVariant) state.getBlock()).onShovel(state, worldIn, pos);
		worldIn.setBlockState(pos, newState);
		if (!player.isCreative() && player instanceof ServerPlayerEntity) {
			if (newState.isSolid())
				pos = pos.up();
			Block.spawnAsEntity(worldIn, pos, new ItemStack(Items.SNOWBALL));
			player.getHeldItemMainhand().damageItem(1, player, stack -> {
				stack.sendBreakAnimation(Hand.MAIN_HAND);
			});
		}
		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
	}

	@SubscribeEvent
	public static void onPlaceBlock(EntityPlaceEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof PlayerEntity) {
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if (SnowCommonConfig.placeSnowInBlock && event.side.isServer() && event.phase == TickEvent.Phase.END && event.world instanceof ServerWorld) {
			WorldTickHandler.tick(event);
		}
	}
}
