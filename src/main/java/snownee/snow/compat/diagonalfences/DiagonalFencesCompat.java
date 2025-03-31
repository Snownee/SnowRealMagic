package snownee.snow.compat.diagonalfences;

import com.google.common.collect.BiMap;

import fuzs.diagonalblocks.api.v2.DiagonalBlockType;
import fuzs.diagonalblocks.api.v2.impl.DiagonalBlockTypeImpl;
import fuzs.diagonalblocks.api.v2.impl.DiagonalFenceBlock;
import fuzs.diagonalfences.DiagonalFences;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowFenceBlock;
import snownee.snow.block.SnowVariant;
import snownee.snow.convert.CoveredBlockConverter;

@NotNullByDefault
@KiwiModule(value = DiagonalFencesCompat.ID, dependencies = DiagonalFencesCompat.ID)
public class DiagonalFencesCompat extends AbstractModule {
	public static final String ID = "diagonalfences";
	public static final DiagonalBlockType TYPE = new DiagonalBlockTypeImpl(
			"snowfences",
			SnowFenceBlock.class,
			SnowDiagonalFenceBlock::new,
			CrossCollisionBlock.NORTH,
			CrossCollisionBlock.EAST,
			CrossCollisionBlock.WEST,
			CrossCollisionBlock.SOUTH,
			CrossCollisionBlock.WATERLOGGED,
			SnowVariant.OPTIONAL_LAYERS) {
		@Override
		public ResourceLocation id(String path) {
			return DiagonalFences.id(path);
		}
	};

	public static void init() {
		DiagonalBlockType.register(TYPE);
	}

	public static BiMap<Block, Block> getBlockConversions() {
		return TYPE.getBlockConversions();
	}

	@Override
	protected void postInit(PostInitEvent event) {
		event.enqueueWork(() -> CoreModule.CONVERTERS.converters.putBefore(
				SnowRealMagic.id("fence"),
				ResourceLocation.fromNamespaceAndPath(ID, "fence"),
				new CoveredBlockConverter(DiagonalFenceBlock.class, Blocks.AIR.defaultBlockState()) {
					@Override
					public boolean accept(BlockState blockState) {
						return TYPE.getBlockConversions().containsKey(normalFenceOf(blockState)) && super.accept(blockState);
					}

					@Override
					public BlockState result(BlockState blockState) {
						Block block = normalFenceOf(blockState);
						return TYPE.getBlockConversions().getOrDefault(block, block).defaultBlockState();
					}

					private Block normalFenceOf(BlockState blockState) {
						//noinspection deprecation
						return blockState.getSoundType() == SoundType.WOOD || blockState.is(BlockTags.WOODEN_FENCES) ?
								CoreModule.FENCE.get() :
								CoreModule.FENCE2.get();
					}
				}));
	}
}
