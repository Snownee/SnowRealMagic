/*
package snownee.snow.compat.terraforged;

import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.snow.CoreModule;
import snownee.snow.ModUtil;
import snownee.snow.world.gen.feature.ModIceAndSnowFeature;

@KiwiModule(value = "terraforged", dependencies = "terraforged")
@KiwiModule.Subscriber
public class TerraForgedModule extends AbstractModule {
	public static void freezeGround(LevelAccessor world, ChunkAccess chunk, Biome biome, MutableBlockPos snowPos, MutableBlockPos underPos) {
		if (!biome.shouldSnow(world, snowPos) && ModIceAndSnowFeature.placeAdditional(biome, world, snowPos)) {
			if (chunk instanceof ProtoChunk) {
				((ProtoChunk) chunk).pendingBlockEntities.remove(snowPos);
			}
			BlockState blockstate = world.getBlockState(underPos);
			if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
				world.setBlock(underPos, blockstate.setValue(SnowyDirtBlock.SNOWY, true), 2);
			}
		}
	}

	public static boolean isIn(BlockState state, Block block) {
		return state.getBlock() instanceof SnowLayerBlock;
	}

	@Override
	protected void preInit() {
		ModUtil.terraforged = true;
	}

	@SubscribeEvent
	public void setupLayers(SetupEvent.Layers event) {
		event.getManager().register(LayerMaterial.of(Blocks.SNOW_BLOCK, CoreModule.TILE_BLOCK));
	}
}
*/