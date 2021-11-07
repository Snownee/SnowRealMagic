package snownee.snow.block;

import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MushroomBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.SnowClientConfig;
import snownee.snow.SnowCommonConfig;
import snownee.snow.entity.FallingSnowEntity;

public class ModSnowBlock extends SnowBlock implements SnowVariant {
	public static final VoxelShape[] SNOW_SHAPES_MAGIC = new VoxelShape[] { VoxelShapes.empty(), Block.makeCuboidShape(0, 0, 0, 16, 1, 16), Block.makeCuboidShape(0, 0, 0, 16, 2, 16), Block.makeCuboidShape(0, 0, 0, 16, 3, 16), Block.makeCuboidShape(0, 0, 0, 16, 4, 16), Block.makeCuboidShape(0, 0, 0, 16, 5, 16), Block.makeCuboidShape(0, 0, 0, 16, 6, 16), Block.makeCuboidShape(0, 0, 0, 16, 7, 16) };

	public ModSnowBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (ModUtil.terraforged || !SnowCommonConfig.thinnerBoundingBox) {
			return super.getCollisionShape(state, worldIn, pos, context);
		}
		int layers = state.get(LAYERS);
		if (layers == 8) {
			return VoxelShapes.fullCube();
		}
		return SNOW_SHAPES_MAGIC[layers - 1];
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, tickRate());
		}
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (SnowCommonConfig.snowGravity) {
			worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, tickRate());
			return stateIn;
		} else {
			return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return isValidPosition(state, worldIn, pos, false);
	}

	public boolean isValidPosition(BlockState state, IBlockReader worldIn, BlockPos pos, boolean ignoreSelf) {
		BlockState blockstate = worldIn.getBlockState(pos.down());
		Block block = blockstate.getBlock();
		if (block instanceof SnowBlock && blockstate.get(LAYERS) == 8) {
			return true;
		} else if ((SnowCommonConfig.snowOnIce && (block == Blocks.ICE || block == Blocks.PACKED_ICE)) || !block.isIn(CoreModule.INVALID_SUPPORTERS)) {
			if (ignoreSelf || state.getMaterial().isReplaceable() || canContainState(state)) {
				if (block.isIn(BlockTags.LEAVES) || Block.doesSideFillSquare(blockstate.getCollisionShape(worldIn, pos.down()), Direction.UP)) {
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
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		checkFallable(worldIn, pos, state);
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (ModUtil.shouldMelt(worldIn, pos)) {
			int layers = state.get(LAYERS);
			if (layers == 8) {
				BlockState upState = worldIn.getBlockState(pos.up());
				if (upState.getBlock() instanceof SnowBlock) {
					return;
				}
			}
			if (state.getBlock() == CoreModule.TILE_BLOCK) {
				state.removedByPlayer(worldIn, pos, null, false, null);
			} else {
				spawnDrops(state, worldIn, pos);
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
		BlockPos height = worldIn.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
		if (height.getY() != pos.getY()) {
			return;
		}

		Biome biome = worldIn.getBiome(pos);
		boolean flag = false;
		if (worldIn.isRaining() && ModUtil.isColdAt(worldIn, biome, pos)) {
			if (SnowCommonConfig.snowAccumulationDuringSnowfall) {
				flag = true;
			} else if (SnowCommonConfig.snowAccumulationDuringSnowstorm && worldIn.isThundering()) {
				flag = true;
			}
		}

		int layers = state.get(LAYERS);
		if (flag && layers < SnowCommonConfig.snowAccumulationMaxLayers) {
			accumulate(worldIn, pos, state, (w, p) -> (SnowCommonConfig.snowAccumulationMaxLayers > 8 || !(w.getBlockState(p.down()).getBlock() instanceof ModSnowBlock)) && w.getLightFor(LightType.BLOCK, p) < 10, true);
		} else if (!SnowCommonConfig.snowNeverMelt && SnowCommonConfig.snowNaturalMelt && !worldIn.isRaining()) {
			if (layers == 1) {
				if (SnowCommonConfig.snowAccumulationMaxLayers > 8 && worldIn.getBlockState(pos.down()).getBlock() instanceof ModSnowBlock) {
					worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
				}
			} else {
				accumulate(worldIn, pos, state, (w, p) -> !(w.getBlockState(p.up()).getBlock() instanceof ModSnowBlock), false);
			}
		}
	}

	private static void accumulate(World world, BlockPos pos, BlockState centerState, BiPredicate<IWorld, BlockPos> filter, boolean accumulate) {
		int i = centerState.get(LAYERS);
		for (int j = 0; j < 8; j++) {
			int k = j / 2;
			Direction direction = Direction.byHorizontalIndex(k);
			BlockPos pos2 = pos.offset(direction);
			if (j % 2 == 1) {
				pos2 = pos2.offset(Direction.byHorizontalIndex(k + 1));
			}
			if (!world.isBlockPresent(pos2) || !filter.test(world, pos2)) {
				continue;
			}
			BlockState state = world.getBlockState(pos2);
			BlockPos height = world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos2);
			if (height.getY() != pos2.getY()) {
				continue;
			}

			if (!CoreModule.BLOCK.isValidPosition(state, world, pos2)) {
				continue;
			}
			int l;
			if (state.getBlock() instanceof SnowBlock) {
				l = state.get(LAYERS);
			} else {
				l = 0;
			}
			if (accumulate ? i > l : i < l) {
				if (accumulate) {
					placeLayersOn(world, pos2, 1, false, new DirectionalPlaceContext(world, pos2, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
				} else {
					world.setBlockState(pos2, state.with(LAYERS, l - 1));
				}
				return;
			}
		}
		if (accumulate) {
			placeLayersOn(world, pos, 1, false, new DirectionalPlaceContext(world, pos, Direction.UP, ItemStack.EMPTY, Direction.DOWN), false);
		} else {
			world.setBlockState(pos, centerState.with(LAYERS, i - 1));
		}
	}

	protected boolean checkFallable(World worldIn, BlockPos pos, BlockState state) {
		BlockPos posDown = pos.down();
		if ((worldIn.isAirBlock(posDown) || canFallThrough(worldIn.getBlockState(posDown), worldIn, posDown)) && pos.getY() >= 0) {
			if (!worldIn.isRemote) {
				worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
				FallingSnowEntity entity = new FallingSnowEntity(worldIn, pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, state.get(LAYERS));
				worldIn.addEntity(entity);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean placeLayersOn(World world, BlockPos pos, int layers, boolean fallingEffect, BlockItemUseContext useContext, boolean playSound) {
		layers = MathHelper.clamp(layers, 1, 8);
		BlockState state = world.getBlockState(pos);
		int originLayers = 0;
		if (state.getBlock() instanceof SnowBlock) {
			originLayers = state.get(LAYERS);
			world.setBlockState(pos, state.with(LAYERS, MathHelper.clamp(originLayers + layers, 1, 8)));
		} else if (canContainState(state) && state.isValidPosition(world, pos)) {
			convert(world, pos, state, MathHelper.clamp(layers, 1, 8), 3);
		} else if (CoreModule.BLOCK.isValidPosition(state, world, pos)) {
			world.setBlockState(pos, CoreModule.BLOCK.getDefaultState().with(LAYERS, MathHelper.clamp(layers, 1, 8)));
		} else {
			return false;
		}
		if (fallingEffect) {
			world.addBlockEvent(pos, CoreModule.BLOCK, originLayers, layers);
		} else if (playSound) {
			SoundType soundtype = CoreModule.BLOCK.getSoundType(CoreModule.BLOCK.getDefaultState());
			world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		}
		if (originLayers + layers > 8) {
			pos = pos.up();
			if (CoreModule.BLOCK.isValidPosition(CoreModule.BLOCK.getDefaultState(), world, pos) && world.getBlockState(pos).isReplaceable(useContext)) {
				placeLayersOn(world, pos, layers - (8 - originLayers), fallingEffect, useContext, playSound);
			}
		}
		return true;
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		int i = state.get(LAYERS);
		if (useContext.getItem().getItem() == CoreModule.BLOCK.asItem() && i < 8) {
			if (useContext.replacingClickedOnBlock() && state.getBlock() == CoreModule.BLOCK) {
				return useContext.getFace() == Direction.UP;
			} else {
				return true;
			}
		}
		return (SnowCommonConfig.snowAlwaysReplaceable && state.get(LAYERS) < 8) || i == 1;
	}

	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int originLayers, int layers) {
		double offsetY = originLayers / 8D;
		layers *= 10;
		for (int i = 0; i < layers; ++i) {
			// TODO better particle
			double d0 = RANDOM.nextGaussian() * 0.1D;
			double d1 = RANDOM.nextGaussian() * 0.02D;
			double d2 = RANDOM.nextGaussian() * 0.1D;
			worldIn.addParticle(ParticleTypes.SPIT, pos.getX() + RANDOM.nextFloat(), pos.getY() + offsetY, pos.getZ() + RANDOM.nextFloat(), d0, d1, d2);
		}
		SoundType soundtype = getSoundType(state, worldIn, pos, null);
		worldIn.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (!SnowClientConfig.particleThroughLeaves || rand.nextInt(32) > 0) {
			return;
		}
		Entity entity = Minecraft.getInstance().getRenderViewEntity();
		if (entity != null && entity.getPosition().distanceSq(pos) > 256) {
			return;
		}
		BlockState stateDown = worldIn.getBlockState(pos.down());
		if (stateDown.getBlock().isIn(BlockTags.LEAVES)) {
			double d0 = pos.getX() + rand.nextDouble();
			double d1 = pos.getY() - 0.05D;
			double d2 = pos.getZ() + rand.nextDouble();
			worldIn.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, stateIn), d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public int getDustColor(BlockState state) {
		return 0xffffffff;
	}

	public static boolean canFallThrough(BlockState state, World worldIn, BlockPos pos) {
		if (FallingBlock.canFallThrough(state) && state.getCollisionShape(worldIn, pos).isEmpty()) {
			return true;
		}
		if (state.getBlock() instanceof SnowBlock && state.get(LAYERS) < 8) {
			return true;
		}
		return false;
	}

	@Override
	public BlockState onShovel(BlockState state, World world, BlockPos pos) {
		int layers = state.get(LAYERS) - 1;
		if (layers > 0) {
			return state.with(LAYERS, layers);
		} else {
			return getRaw(state, world, pos);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) {
			BlockState stateDown = worldIn.getBlockState(pos.down());
			if (!(stateDown.getBlock() instanceof SnowBlock) && !stateDown.hasProperty(BlockStateProperties.SNOWY)) {
				if (state.getBlock() == CoreModule.BLOCK) {
					worldIn.setBlockState(pos, copyProperties(state, CoreModule.TILE_BLOCK.getDefaultState()), 16 | 32);
				}
				TileEntity blockEntity = worldIn.getTileEntity(pos);
				if (blockEntity instanceof SnowTile) {
					SnowTile snowTile = (SnowTile) blockEntity;
					if (state.getBlock() == CoreModule.TILE_BLOCK && snowTile.getState().isAir()) {
						worldIn.setBlockState(pos, copyProperties(state, CoreModule.BLOCK.getDefaultState()), 16 | 32);
					} else {
						snowTile.options.renderOverlay = !snowTile.options.renderOverlay;
						if (worldIn.isRemote) {
							worldIn.markAndNotifyBlock(pos, worldIn.getChunkAt(pos), state, state, 11, 512);
						}
					}
				}
				return ActionResultType.SUCCESS;
			}
		}
		if (state.getBlock() == CoreModule.BLOCK) {
			BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(player, handIn, hit));
			Block block = Block.getBlockFromItem(context.getItem().getItem());
			if (block != null && block != Blocks.AIR && context.replacingClickedOnBlock()) {
				BlockState state2 = block.getStateForPlacement(context);
				if (state2 != null && canContainState(state2) && state2.isValidPosition(worldIn, pos)) {
					if (!worldIn.isRemote) {
						worldIn.setBlockState(pos, state2, 16 | 32);
						block.onBlockPlacedBy(worldIn, pos, state, player, context.getItem());
						int i = state.get(LAYERS);
						if (placeLayersOn(worldIn, pos, i, false, context, true) && !player.isCreative()) {
							context.getItem().shrink(1);
						}
					}
					return ActionResultType.SUCCESS;
				}
			}
		}
		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = context.getWorld().getBlockState(context.getPos());
		if (blockstate.getBlock() instanceof SnowBlock) {
			int i = blockstate.get(LAYERS);
			return blockstate.with(LAYERS, Math.min(8, i + 1));
		} else {
			return super.getStateForPlacement(context);
		}
	}

	public static boolean canContainState(BlockState state) {
		if (!SnowCommonConfig.placeSnowInBlock || state.getBlock().hasTileEntity(state) || !state.getFluidState().isEmpty()) {
			return false;
		}
		Block block = state.getBlock();
		if (block.isIn(CoreModule.NOT_CONTAINABLES)) {
			return false;
		}
		if (block.isIn(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			return true;
		}
		if (block instanceof FenceBlock && block.isIn(BlockTags.FENCES)) {
			return hasAllProperties(state, CoreModule.FENCE.getDefaultState());
		}
		if (block instanceof FenceGateBlock && block.isIn(BlockTags.FENCE_GATES)) {
			return hasAllProperties(state, CoreModule.FENCE_GATE.getDefaultState());
		}
		if (block instanceof WallBlock && block.isIn(BlockTags.WALLS)) {
			return hasAllProperties(state, CoreModule.WALL.getDefaultState());
		}
		if (block instanceof SlabBlock && state.get(SlabBlock.TYPE) == SlabType.BOTTOM && block.isIn(BlockTags.SLABS)) {
			return true;
		}
		if (block instanceof StairsBlock && state.get(StairsBlock.HALF) == Half.BOTTOM && block.isIn(BlockTags.STAIRS)) {
			return hasAllProperties(state, CoreModule.STAIRS.getDefaultState());
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public static boolean convert(IWorld world, BlockPos pos, BlockState state, int layers, int flags) {
		if (!SnowCommonConfig.placeSnowInBlock || state.hasTileEntity()) {
			return false;
		}
		Block block = state.getBlock();
		if (state.isAir()) {
			world.setBlockState(pos, CoreModule.BLOCK.getDefaultState().with(LAYERS, layers), flags);
			return true;
		}
		if (block.isIn(CoreModule.CONTAINABLES) || block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof FlowerBlock || block instanceof SaplingBlock || block instanceof MushroomBlock || block instanceof SweetBerryBushBlock) {
			world.setBlockState(pos, CoreModule.TILE_BLOCK.getDefaultState().with(LAYERS, layers), flags);
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof SnowTile) {
				((SnowTile) tile).setState(state);
			}
			return true;
		}

		BlockPos posDown = pos.down();
		BlockState stateDown = world.getBlockState(posDown);
		if (block instanceof StairsBlock && block != CoreModule.STAIRS && block.isIn(BlockTags.STAIRS)) {
			BlockState newState = CoreModule.STAIRS.getDefaultState();
			newState = copyProperties(state, newState);
			world.setBlockState(pos, newState, flags);
		} else if (block instanceof SlabBlock && block != CoreModule.SLAB && state.get(SlabBlock.TYPE) == SlabType.BOTTOM && block.isIn(BlockTags.SLABS)) {
			// can't copy properties as this doesn't extend vanilla slabs
			world.setBlockState(pos, CoreModule.SLAB.getDefaultState(), flags);
		} else if (block instanceof FenceBlock && block.getClass() != SnowFenceBlock.class && block.isIn(BlockTags.FENCES)) {
			Block newBlock = block.isIn(BlockTags.WOODEN_FENCES) ? CoreModule.FENCE : CoreModule.FENCE2;
			BlockState newState = newBlock.getDefaultState();
			newState = copyProperties(state, newState);
			newState = newState.updatePostPlacement(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlockState(pos, newState, flags);
		} else if (block instanceof FenceGateBlock && block != CoreModule.FENCE_GATE && block.isIn(BlockTags.FENCE_GATES)) {
			BlockState newState = CoreModule.FENCE_GATE.getDefaultState();
			newState = copyProperties(state, newState);
			newState = newState.updatePostPlacement(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlockState(pos, newState, flags);
		} else if (block instanceof WallBlock && block != CoreModule.WALL && block.isIn(BlockTags.WALLS)) {
			BlockState newState = CoreModule.WALL.getDefaultState();
			newState = copyProperties(state, newState);
			newState = newState.updatePostPlacement(Direction.DOWN, stateDown, world, pos, posDown);
			world.setBlockState(pos, newState, flags);
		} else {
			return false;
		}

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof SnowTile) {
			((SnowTile) tile).setState(state);
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
			newState = newState.with(property, property.getValueClass().cast(entry.getValue()));
		}
		return newState;
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
		if (SnowCommonConfig.snowReduceFallDamage) {
			BlockState state = worldIn.getBlockState(pos.down());
			if (!state.isIn(this))
				return;
			if (state.getBlock() == CoreModule.BLOCK || state.getBlock() == CoreModule.TILE_BLOCK) {
				entityIn.onLivingFall(fallDistance, 0.2F);
				return;
			}
			state = worldIn.getBlockState(pos);
			entityIn.onLivingFall(fallDistance, 1 - state.get(LAYERS) * 0.1F);
			return;
		}
		super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
	}

	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		if (SnowCommonConfig.thinnerBoundingBox) {
			double d0 = Math.abs(entityIn.getMotion().y);
			if (d0 < 0.1D && !entityIn.isSteppingCarefully()) {
				BlockState state = worldIn.getBlockState(pos);
				if (!state.isIn(this))
					return;
				int layers = state.get(LAYERS) - 1;
				double d1 = 1 - layers * 0.05f;
				entityIn.setMotion(entityIn.getMotion().mul(d1, 1.0D, d1));
			}
		}
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return super.getPickBlock(state, target, world, pos, player);
	}

}
