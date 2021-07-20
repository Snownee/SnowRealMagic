package snownee.snow;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class WorldEvents {
	private WorldEvents() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void populateIce(PopulateChunkEvent.Populate event) {
		if (!ModConfig.placeSnowInBlock || !ModConfig.replaceSnowWorldGen || event.getType() != EventType.ICE)
			return;
		event.setResult(Result.DENY);
		World world = event.getWorld();
		for (int k2 = 0; k2 < 16; ++k2) {
			for (int j3 = 0; j3 < 16; ++j3) {
				BlockPos blockpos1 = world.getPrecipitationHeight(new BlockPos(event.getChunkX() * 16 + 8 + k2, 0, event.getChunkZ() * 16 + 8 + j3));
				BlockPos blockpos2 = blockpos1.down();

				if (world.canBlockFreezeWater(blockpos2)) {
					world.setBlockState(blockpos2, Blocks.ICE.getDefaultState(), 2);
				}

				trySnowAt(world, blockpos1, true);
			}
		}
	}

	public static void trySnowAt(World world, BlockPos pos, boolean checkLight) {
		Biome biome = world.getBiome(pos);
		float f = biome.getTemperature(pos);

		if (f >= 0.15F) {
			return;
		}
		if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
			if (Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos)) {
				ModSnowBlock.placeLayersOn(world, pos, 1, false, false);
			}
		}
	}
}
