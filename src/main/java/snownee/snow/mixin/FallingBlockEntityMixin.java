package snownee.snow.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.snow.Hooks;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.SnowVariant;
import snownee.snow.network.SLavaSmokeEffectPacket;
import snownee.snow.util.CommonProxy;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
	@Unique
	@Nullable
	private BlockPos srm$oBlockPos;
	@Shadow
	private BlockState blockState;
	@Shadow
	private boolean hurtEntities;
	@Shadow
	public boolean dropItem;

	public FallingBlockEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/level/block/state/BlockState;)V", at = @At("RETURN"))
	private void srm_init(Level level, double x, double y, double z, BlockState blockState, CallbackInfo ci) {
		if (this.blockState.is(Blocks.SNOW)) {
			hurtEntities = false;
			dropItem = false;
		}
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;blockPosition()Lnet/minecraft/core/BlockPos;"),
			cancellable = true)
	private void srm_touchLiquid(CallbackInfo ci) {
		if (!blockState.is(Blocks.SNOW)) {
			return;
		}
		BlockPos pos = this.blockPosition();
		if (srm$oBlockPos != null && srm$oBlockPos.equals(pos)) {
			return;
		}
		srm$oBlockPos = pos;
		BlockState blockState = level().getBlockState(pos);
		boolean handled = true;
		if (SnowCommonConfig.snowMakingIce && blockState.is(Blocks.WATER) && blockState.getFluidState().isSource()) {
			level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
		} else if (CommonProxy.isHot(blockState.getFluidState(), level(), pos)) {
			new SLavaSmokeEffectPacket(pos.above()).sendToAround((ServerLevel) level());
		} else if (blockState.getFluidState().isEmpty()) {
			handled = false;
		}
		if (handled) {
			discard();
			ci.cancel();
		}
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;canBeReplaced(Lnet/minecraft/world/item/context/BlockPlaceContext;)Z"),
			cancellable = true)
	private void srm_touchGround(CallbackInfo ci) {
		if (!blockState.is(Blocks.SNOW)) {
			return;
		}
		BlockPos pos = getOnPos();
		BlockState blockIn = level().getBlockState(pos);
		if (!Hooks.canPlaceAt(level(), pos) && !(blockIn.getBlock() instanceof SnowVariant)) {
			pos = blockPosition();
		}
		Hooks.placeLayersOn(
				level(),
				pos,
				blockState.getValue(SnowLayerBlock.LAYERS),
				true,
				new DirectionalPlaceContext(level(), pos, Direction.DOWN, ItemStack.EMPTY, Direction.UP),
				true,
				true);
		discard();
		ci.cancel();
	}
}
