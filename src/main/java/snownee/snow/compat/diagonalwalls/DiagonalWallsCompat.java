package snownee.snow.compat.diagonalwalls;

import com.google.common.collect.BiMap;

import fuzs.diagonalblocks.api.v2.DiagonalBlockType;
import fuzs.diagonalblocks.api.v2.client.MultiPartTranslator;
import fuzs.diagonalblocks.api.v2.impl.DiagonalBlockTypeImpl;
import fuzs.diagonalblocks.api.v2.impl.DiagonalWallBlock;
import fuzs.diagonalblocks.client.resources.model.WallMultiPartTranslator;
import fuzs.diagonalwalls.DiagonalWalls;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.NotNullByDefault;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.convert.CoveredBlockConverter;

@NotNullByDefault
@KiwiModule(value = DiagonalWallsCompat.ID, dependencies = DiagonalWallsCompat.ID)
public class DiagonalWallsCompat extends AbstractModule {
	public static final String ID = "diagonalwalls";
	public static final DiagonalBlockType TYPE = new DiagonalBlockTypeImpl(
			"snowwalls",
			SnowWallBlock.class,
			SnowDiagonalWallBlock::new,
			WallBlock.UP,
			WallBlock.NORTH_WALL,
			WallBlock.EAST_WALL,
			WallBlock.WEST_WALL,
			WallBlock.SOUTH_WALL,
			WallBlock.WATERLOGGED,
			SnowVariant.OPTIONAL_LAYERS) {
		@Override
		public ResourceLocation id(String path) {
			return DiagonalWalls.id(path);
		}
	};

	public static void init() {
		DiagonalBlockType.register(TYPE);
		if (Platform.isPhysicalClient()) {
			MultiPartTranslator.register(TYPE, new WallMultiPartTranslator());
		}
	}

	public static BiMap<Block, Block> getBlockConversions() {
		return TYPE.getBlockConversions();
	}

	@Override
	protected void postInit(PostInitEvent event) {
		event.enqueueWork(() -> CoreModule.CONVERTERS.converters.putBefore(
				SnowRealMagic.id("wall"),
				ResourceLocation.fromNamespaceAndPath(ID, "wall"),
				new CoveredBlockConverter(DiagonalWallBlock.class, CoreModule.WALL.defaultBlockState()) {
					@Override
					public boolean accept(BlockState blockState) {
						Block block = result.getBlock();
						return TYPE.getBlockConversions().containsKey(block) && super.accept(blockState);
					}

					@Override
					public BlockState result(BlockState blockState) {
						Block block = result.getBlock();
						return TYPE.getBlockConversions().getOrDefault(block, block).defaultBlockState();
					}
				}));
	}
}
