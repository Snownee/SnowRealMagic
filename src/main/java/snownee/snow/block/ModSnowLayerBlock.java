package snownee.snow.block;

import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.KiwiGO;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowCommonConfig;
import snownee.snow.block.entity.SnowBlockEntity;
import snownee.snow.client.SnowClientConfig;
import snownee.snow.entity.FallingSnowEntity;

public class ModSnowLayerBlock extends SnowLayerBlock implements SnowVariant {
	public static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[] { Shapes.empty(), Block.box(0, 0, 0, 16, 1, 16), Block.box(0, 0, 0, 16, 2, 16), Block.box(0, 0, 0, 16, 3, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 0, 0, 16, 5, 16), Block.box(0, 0, 0, 16, 6, 16), Block.box(0, 0, 0, 16, 7, 16) };

	public ModSnowLayerBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (ModUtil.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
			return super.getCollisionShape(state, worldIn, pos, context);
		}
		int layers = state.getValue(LAYERS);
		if (layers == 8) {
			return Shapes.block();
		}
		return SNOW_SHAPES_MAGIC[layers - 1];
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.scheduleTick(pos, this, tickRate());
		}
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.scheduleTick(currentPos, this, tickRate());
			return stateIn;
		} else {
			return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return canSurvive(state, worldIn, pos, false);
	}

	public boolean canSurvive(BlockState state, BlockGetter worldIn, BlockPos pos, boolean ignoreSelf) {
		BlockState blockstate = worldIn.getBlockState(pos.below());
		Block block = blockstate.getBlock();
		if (block instanceof SnowLayerBlock && blockstate.getValue(LAYERS) == 8) {
			return true;
		} else if ((SnowCommonConfig.snowOnIce && (blockstate.is(Blocks.ICE) || blockstate.is(Blocks.PACKED_ICE))) || !blockstate.is(CoreModule.INVALID_SUPPORTERS)) {
			if (ignoreSelf || state.getMaterial().isReplaceable() || canContainState(state)) {
				if (blockstate.is(BlockTags.LEAVES) || Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP)) {
					return true;
				}
			}
		}
		return false;
	}

	protected int tickRate() {
		return 2;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		checkFallable(worldIn, pos, state);
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		if (ModUtil.shouldMelt(worldIn, pos)) {
			int layers = state.getValue(LAYERS);
			if (layers == 8) {
				BlockState upState = worldIn.getBlockState(pos.above());
				if (upState.getBlock() instanceof SnowLayerBlock) {
					return;
				}
			}
			if (CoreModule.TILE_BLOCK.is(state)) {
				state.onDestroyedByPlayer(worldIn, pos, null, false, null);
			} else {
				dropResources(state, worldIn, pos);
				worldIn.removeBlock(pos, false);
			}
			return;
		}
		if (ModUtil.terraforged) {
			return;
		}
		if (!SnowCommonConfig.snowAccumulationDuringSnowfall && !SnowCommonConfig.snowAccumulationDuringSnowstorm) {
			return;
		}
		if (random.nextInt(8) > 0) {
			return;
		}
		int layers = state.getValue(LAYERS);
		BlockPos height = worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
		if (layers == 8) {
			if (height.getY() - 1 != pos.getY()) {
				return;
			}
			BlockState upState = worldIn.getBlockState(pos.above());
			if (upState.getBlock() instanceof SnowLayerBlock) {
				return;
			}
		} else {
			if (height.getY() != pos.getY()) {
				return;
			}
		}

		Holder<Biome> biome = worldIn.getBiome(pos);
		boolean flag = false;
		if (worldIn.isRaining() && ModUtil.coldEnoughToSnow(worldIn, pos, biome)) {
			if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
				flag = true;
			} else if (SnowCommonConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering()) {
				flag = true;
			}
		}

		if (flag && layers < SnowCommonConfig.snowAccumulationMaxLayers) {
			accumulate(worldIn, pos, state, (w, p) -> (SnowCommonConfig.snowAccumulationMaxLayers > 8 || !(w.getBlockState(p.below()).getBlock() instanceof ModSnowLayerBlock)) && w.getBrightness(LightLayer.BLOCK, p) < 10, true);
		} else if (!SnowCommonConfig.snowNeverMelt && SnowCommonConfig.snowNaturalMelt && !worldIn.isRaining()) {
			if (layers == 1) {
				if (SnowCommonConfig.snowAccumulationMaxLayers > 8 && worldIn.getBlockState(pos.below()).getBlock() instanceof ModSnowLayerBlock) {
					worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
				}
			} else {
				accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.above()).getBlock() instanceof ModSnowLayerBlock), false);
			}
		}
	}

	private static void accumulate(Level world, BlockPos pos, BlockState centerState, BiPredicate<LevelAccessor, BlockPos> filter, boolean accumulate) {
		int i = centerState.getValue(LAYERS);
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			Direction direction = Direction.from2DDataValue(k);
			BlockPos pos2 = pos.relative(direction);
			if (j % 2 == 1) {
				pos2 = pos2.relative(Direction.from2DDataValue(k + 1));
			}
			if (!world.isLoaded(pos2) || !filter.test(world, pos2)) {
				continue;
			}
			BlockState state = world.getBlockState(pos2);
			BlockPos height = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos2);
			if (height.getY() != pos2.getY()) {
				continue;
			}

			if (!CoreModule.BLOCK.get().canSurvive(state, world, pos2)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof SnowLayerBlock) {
				l = state.getValue(LAYERS);
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				if (accumulate) {
					placeLayersOn(world, pos2, 1, false, new DirectionalPlaceContext(world, pos2, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
				} else {
					world.setBlockAndUpdate(pos2, state.setValue(LAYERS, l - 1));
				}
				return;
			}
		}
		if (accumulate) {
			placeLayersOn(world, pos, 1, false, new DirectionalPlaceContext(world, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
		} else {
			world.setBlockAndUpdate(pos, centerState.setValue(LAYERS, i - 1));
		}
	}

	protected boolean checkFallable(Level worldIn, BlockPos pos, BlockState state) {
		BlockPos posDown = pos.below();
		if (canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) {
			if (!worldIn.isClientSide) {
				worldIn.setBlockAndUpdate(pos, getRaw(state, worldIn, pos));
				FallingSnowEntity entity = new FallingSnowEntity(worldIn, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, state.getValue(LAYERS));
				worldIn.addFreshEntity(entity);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean placeLayersOn(Level world, BlockPos pos, int layers, boolean fallingEffect, BlockPlaceContext useContext, boolean playSound) {
		layers = Mth.clamp(layers, 1, 8);
		BlockState state = world.getBlockState(pos);
		int originLayers = 0;
		if (state.getBlock() instanceof SnowLayerBlock) {
			originLayers = state.getValue(LAYERS);
			world.setBlockAndUpdate(pos, state.setValue(LAYERS, Mth.clamp(originLayers + layers, 1, 8)));
		} else if (canContainState(state) && state.canSurvive(world, pos)) {
			convert(world, pos, state, Mth.clamp(layers, 1, 8), 3);
		} else if (CoreModule.BLOCK.get().canSurvive(state, world, pos)) {
			world.setBlockAndUpdate(pos, CoreModule.BLOCK.defaultBlockState().setValue(LAYERS, Mth.clamp(layers, 1, 8)));
		} else {
			return false;
		}
		if (fallingEffect) {
			world.blockEvent(pos, CoreModule.BLOCK.get(), originLayers, layers);
		} else if (playSound) {
			SoundType soundtype = CoreModule.BLOCK.get().getSoundType(CoreModule.BLOCK.defaultBlockState());
			world.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		}
		if (originLayers + layers > 8) {
			pos = pos.above();
			if (CoreModule.BLOCK.get().canSurvive(CoreModule.BLOCK.defaultBlockState(), world, pos) && world.getBlockState(pos).canBeReplaced(useContext)) {
				placeLayersOn(world, pos, layers - (8 - originLayers), fallingEffect, useContext, playSound);
			}
		}
		return true;
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		int i = state.getValue(LAYERS);
		if (CoreModule.BLOCK.is(useContext.getItemInHand()) && i < 8) {
			if (useContext.replacingClickedOnBlock() && CoreModule.BLOCK.is(state)) {
				return useContext.getClickedFace() == Direction.UP;
			} else {
				return true;
			}
		}
		return (SnowCommonConfig.snowAlwaysReplaceable && state.getValue(LAYERS) < 8) || i == 1;
	}

	@Override
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int originLayers, int layers) {
		double offsetY = originLayers / 8D;
		layers *= 10;
		for (int i = 0; i < layers; ++i) {
			double d0 = RANDOM.nextGaussian() * 0.1D;
			double d1 = RANDOM.nextGaussian() * 0.02D;
			double d2 = RANDOM.nextGaussian() * 0.1D;
			worldIn.addParticle(ParticleTypes.SNOWFLAKE, pos.getX() + RANDOM.nextFloat(), pos.getY() + offsetY, pos.getZ() + RANDOM.nextFloat(), d0, d1, d2);
		}
		SoundType soundtype = getSoundType(state, worldIn, pos, null);
		worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		if (!SnowClientConfig.particleThroughLeaves || rand.nextInt(32) > 0) {
			return;
		}
		Entity entity = Minecraft.getInstance().getCameraEntity();
		if (entity != null && entity.blockPosition().distSqr(pos) > 256) {
			return;
		}
		BlockState stateDown = worldIn.getBlockState(pos.below());
		if (stateDown.is(BlockTags.LEAVES)) {
			double d0 = pos.getX() + rand.nextDouble();
			double d1 = pos.getY() - 0.05D;
			double d2 = pos.getZ() + rand.nextDouble();
			worldIn.addParticle(ParticleTypes.SNOWFLAKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public int getDustColor(BlockState state) {
		return 0xffffffff;
	}

	public static boolean canFallThrough(BlockState state, Level worldIn, BlockPos pos) {
		if (state.getCollisionShape(worldIn, pos).isEmpty()) {
			if (FallingBlock.isFree(state) || canContainState(state)) {
				return true;
			}
		}
		if (state.getBlock() instanceof SnowLayerBlock && state.getValue(LAYERS) < 8) {
			return true;
		}
		return false;
	}

	@Override
	public BlockState onShovel(BlockState state, Level world, BlockPos pos) {
		int layers = state.getValue(LAYERS) - 1;
		if (layers > 0) {
			return state.setValue(LAYERS, layers);
		} else {
			return getRaw(state, world, pos);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
			BlockState stateDown = worldIn.getBlockState(pos.below());
			if (!(stateDown.getBlock() instanceof SnowLayerBlock) && !stateDown.hasProperty(BlockStateProperties.SNOWY)) {
				if (CoreModule.BLOCK.is(state)) {
					worldIn.setBlock(pos, copyProperties(state, CoreModule.TILE_BLOCK.defaultBlockState()), 16 | 32);
				}
				BlockEntity blockEntity = worldIn.getBlockEntity(pos);
				if (blockEntity instanceof SnowBlockEntity) {
					SnowBlockEntity snowTile = (SnowBlockEntity) blockEntity;
					if (CoreModule.TILE_BLOCK.is(state) && snowTile.getState().isAir()) {
						worldIn.setBlock(pos, copyProperties(state, CoreModule.BLOCK.defaultBlockState()), 16 | 32);
					} else {
						snowTile.options.renderOverlay = !snowTile.options.renderOverlay;
						if (worldIn.isClientSide) {
							worldIn.markAndNotifyBlock(pos, worldIn.getChunkAt(pos), state, state, 11, 512);
						}
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		if (CoreModule.BLOCK.is(state)) {
			BlockPlaceContext context = new BlockPlaceContext(player, handIn, player.getItemInHand(handIn), hit);
			Block block = Block.byItem(context.getItemInHand().getItem());
			if (block != null && block != Blocks.AIR && context.replacingClickedOnBlock()) {
				BlockState state2 = block.getStateForPlacement(context);
				if (state2 != null && canContainState(state2) && state2.canSurvive(worldIn, pos)) {
					if (!worldIn.isClientSide) {
						worldIn.setBlock(pos, state2, 16 | 32);
						block.setPlacedBy(worldIn, pos, state, player, context.getItemInHand());
						int i = state.getValue(LAYERS);
						if (placeLayersOn(worldIn, pos, i, false, context, true) && !player.isCreative()) {
							context.getItemInHand().shrink(1);
						}
					}
					return InteractionResult.SUCCESS;
				}
			}
		}
		return super.use(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
		if (blockstate.getBlock() instanceof SnowLayerBlock) {
			int i = blockstate.getValue(LAYERS);
			return blockstate.setValue(LAYERS, Math.min(8, i + 1));
		}
		ItemStack stack = context.getItemInHand();
		CompoundTag tag = BlockItem.getBlockEntityData(stack);
		if (tag != null && tag.getString("id").equals("snowrealmagic:snow")) {
			return CoreModule.TILE_BLOCK.defaultBlockState();
		}
		return defaultBlockState();
	}

	public static boolean canContainState(BlockState state) {
		if (!SnowCommonConfig.canPlaceSnowInBlock() || state.hasBlockEntity() || !state.getFluidState().isEmpty()) {
			return false;
		}
		Block block = state.getBlock();
		if (state.is(CoreModule.NOT_CONTAINABLES)) {
			return false;
		}
		if (state.is(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			return true;
		}
		if (block instanceof FenceBlock && state.is(BlockTags.FENCES)) {
			return hasAllProperties(state, CoreModule.FENCE.defaultBlockState());
		}
		if (block instanceof FenceGateBlock && state.is(BlockTags.FENCE_GATES)) {
			return hasAllProperties(state, CoreModule.FENCE_GATE.defaultBlockState());
		}
		if (block instanceof WallBlock && state.is(BlockTags.WALLS)) {
			return hasAllProperties(state, CoreModule.WALL.defaultBlockState());
		}
		if (block instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && state.is(BlockTags.SLABS)) {
			return true;
		}
		if (block instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM && state.is(BlockTags.STAIRS)) {
			return hasAllProperties(state, CoreModule.STAIRS.defaultBlockState());
		}
		return false;
	}

	public static boolean convert(LevelAccessor world, BlockPos pos, BlockState state, int layers, int flags) {
		if (!SnowCommonConfig.canPlaceSnowInBlock() || state.hasBlockEntity()) {
			return false;
		}
		Block block = state.getBlock();
		if (state.isAir()) {
			world.setBlock(pos, CoreModule.BLOCK.defaultBlockState().setValue(LAYERS, layers), flags);
			return true;
		}
		if (state.is(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			world.setBlock(pos, CoreModule.TILE_BLOCK.defaultBlockState().setValue(LAYERS, layers), flags);
			BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof SnowBlockEntity) {
				((SnowBlockEntity) tile).setState(state);
			}
			return true;
		}

		BlockPos posDown = pos.below();
		BlockState stateDown = world.getBlockState(posDown);
		if (block instanceof StairBlock && !CoreModule.STAIRS.is(state) && state.is(BlockTags.STAIRS)) {
			BlockState newState = CoreModule.STAIRS.defaultBlockState();
			newState = copyProperties(state, newState);
			world.setBlock(pos, newState, flags);
		} else if (block instanceof SlabBlock && !CoreModule.SLAB.is(state) && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM && state.is(BlockTags.SLABS)) {
			// can't copy properties as this doesn't extend vanilla slabs
			world.setBlock(pos, CoreModule.SLAB.defaultBlockState(), flags);
		} else if (block instanceof FenceBlock && block.getClass() != SnowFenceBlock.class && state.is(BlockTags.FENCES)) {
			KiwiGO<Block> newBlock = state.is(BlockTags.WOODEN_FENCES) ? CoreModule.FENCE : CoreModule.FENCE2;
			BlockState newState = newBlock.defaultBlockState();
			newState = copyProperties(state, newState);
			newState = newState.updateShape(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlock(pos, newState, flags);
		} else if (block instanceof FenceGateBlock && !CoreModule.FENCE_GATE.is(state) && state.is(BlockTags.FENCE_GATES)) {
			BlockState newState = CoreModule.FENCE_GATE.defaultBlockState();
			newState = copyProperties(state, newState);
			newState = newState.updateShape(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlock(pos, newState, flags);
		} else if (block instanceof WallBlock && !CoreModule.WALL.is(state) && state.is(BlockTags.WALLS)) {
			BlockState newState = CoreModule.WALL.defaultBlockState();
			newState = copyProperties(state, newState);
			newState = newState.updateShape(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlock(pos, newState, flags);
		} else {
			return false;
		}

		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof SnowBlockEntity) {
			((SnowBlockEntity) tile).setState(state);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> boolean hasAllProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (!newState.hasProperty(property))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> BlockState copyProperties(BlockState oldState, BlockState newState) {
		for (Map.Entry<Property<?>, Comparable<?>> entry : oldState.getValues().entrySet()) {
			Property<T> property = (Property<T>) entry.getKey();
			if (!newState.hasProperty(property))
				continue;
			newState = newState.setValue(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	@Override
	public void fallOn(Level worldIn, BlockState state, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			if (!state.is(this))
				return;
			//FIXME why
			if (CoreModule.BLOCK.is(state) || CoreModule.TILE_BLOCK.is(state)) {
				entityIn.causeFallDamage(fallDistance, 0.2F, DamageSource.FALL);
				return;
			}
			state = worldIn.getBlockState(pos);
			entityIn.causeFallDamage(fallDistance, 1 - state.getValue(LAYERS) * 0.1F, DamageSource.FALL);
			return;
		}
		super.fallOn(worldIn, state, pos, entityIn, fallDistance);
	} //FIXME what if below block is snow as well

	@Override
	public void stepOn(Level worldIn, BlockPos pos, BlockState state, Entity entityIn) {
		if (SnowCommonConfig.thinnerBoundingBox) {
			double d0 = Math.abs(entityIn.getDeltaMovement().y);
			if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
				if (!state.is(this))
					return;
				int layers = state.getValue(LAYERS) - 1;
				double d1 = 1 - layers * 0.05f;
				entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(d1, 1.0D, d1));
			}
		}
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		ItemStack stack = getRaw(state, world, pos).getCloneItemStack(target, world, pos, player);
		return stack.isEmpty() ? CoreModule.ITEM.itemStack() : stack;
	}

}
