package snownee.snow.compat.diagonalwalls;

import com.google.common.collect.BiMap;

import fuzs.diagonalblocks.common.api.v2.block.DiagonalWallBlock;
import fuzs.diagonalblocks.common.api.v2.block.type.DiagonalBlockType;
import fuzs.diagonalblocks.common.api.v2.block.type.DiagonalBlockTypeImpl;
import fuzs.diagonalblocks.common.api.v2.client.MultiPartTranslator;
import fuzs.diagonalblocks.common.impl.client.resources.translator.WallMultiPartTranslator;
import fuzs.diagonalwalls.common.DiagonalWalls;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.snow.CoreModule;
import snownee.snow.SnowRealMagic;
import snownee.snow.block.SnowVariant;
import snownee.snow.block.SnowWallBlock;
import snownee.snow.convert.CoveredBlockConverter;

@KiwiModule(value = DiagonalWallsCompat.ID, dependencies = DiagonalWallsCompat.ID)
public class DiagonalWallsCompat extends AbstractModule {
	public static final String ID = "diagonalwalls";
	public static final DiagonalBlockType TYPE = new DiagonalBlockTypeImpl(
			"snowwalls",
			SnowWallBlock.class,
			_ -> SnowDiagonalWallBlock::new,
			WallBlock.UP,
			WallBlock.NORTH,
			WallBlock.EAST,
			WallBlock.WEST,
			WallBlock.SOUTH,
			WallBlock.WATERLOGGED,
			SnowVariant.OPTIONAL_LAYERS) {
		@Override
		public Identifier id(String path) {
			return DiagonalWalls.id(path);
		}
	};

	@Override
	public void addRegistries() {
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
				Identifier.fromNamespaceAndPath(ID, "wall"),
				new CoveredBlockConverter(DiagonalWallBlock.class, CoreModule.WALL.defaultBlockState()) {
					@Override
					public boolean accept(BlockState blockState) {
						Block block = result.getBlock();
						return getBlockConversions().containsKey(block) && super.accept(blockState);
					}

					@Override
					public BlockState result(BlockState blockState) {
						Block block = result.getBlock();
						return getBlockConversions().getOrDefault(block, block).defaultBlockState();
					}
				}));
	}
}
