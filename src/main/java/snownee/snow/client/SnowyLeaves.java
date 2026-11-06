package snownee.snow.client;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.snow.SnowAccumulation;
import snownee.snow.SnowAccumulationData;
import snownee.snow.SnowRealMagic;
import snownee.snow.client.model.SnowyLeavesModel;

public final class SnowyLeaves {

	public record Leaf(Block block, boolean biomeTint, int tint) {
	}

	private static final ConcurrentMap<Long, SnowAccumulationData> CHUNK_DATA = new ConcurrentHashMap<>();
	private static @Nullable ResourceKey<Level> cachedDimension;
	private static @Nullable Set<Block> lastSupportedBlocks;
	private static boolean clientReady;
	private static boolean reloadPending;
	private static long reloadRequestedAt;

	private SnowyLeaves() {
	}

	@Nullable
	public static Leaf leaf(Block block) {
		if (!SnowAccumulation.supportedBlocks().contains(block)) {
			return null;
		}
		BlockState state = block.defaultBlockState();
		BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(state, 0);
		if (source == null) {
			return new Leaf(block, false, 0xFFFFFF);
		}
		int tint = source.color(state);
		if (tint == FoliageColor.FOLIAGE_DEFAULT) {
			return new Leaf(block, true, 0xFFFFFF);
		}
		return new Leaf(block, false, tint);
	}

	public static Identifier snowySprite(Block block, Identifier baseTexture, boolean side, int level) {
		Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
		return SnowRealMagic.id("block/snowy_leaves/" + blockId.getNamespace() + "/" + blockId.getPath() + "/" +
				(side ? "side" : "top") + "/" + baseTexture.getNamespace() + "/" + baseTexture.getPath() + "_" + level);
	}

	public static int intensityForLevel(int level) {
		int state = Mth.clamp(level, 0, SnowAccumulation.MAX_STATE);
		List<Number> configured = SnowClientConfig.snowyLeavesStateIntensities;
		Number configuredValue = state < configured.size() ? configured.get(state) : null;
		int intensity = configuredValue == null
				? SnowAccumulation.defaultIntensityForLevel(state)
				: Math.round(configuredValue.floatValue());
		return Math.max(SnowClientConfig.snowyLeavesIntensity, Mth.clamp(intensity, 0, 100));
	}

	public static int snowLevel(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		if (level.getBlockState(pos).getBlock() != state.getBlock()) {
			return 0;
		}
		return snowLevelAt(pos, state);
	}

	private static int snowLevelAt(BlockPos pos, BlockState state) {
		if (!SnowAccumulation.isSupported(state)) {
			return 0;
		}
		SnowAccumulationData data = CHUNK_DATA.get(ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4));
		return data == null ? 0 : data.get(pos.asLong());
	}

	@Nullable
	public static TextureAtlasSprite particleSprite(BlockPos pos, BlockState state) {
		if (!SnowAccumulation.isSupported(state)) {
			return null;
		}
		BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
		if (!(model instanceof SnowyLeavesModel snowyModel)) {
			return null;
		}
		int snowLevel = snowLevelAt(pos, state);
		if (intensityForLevel(snowLevel) <= 0) {
			return null;
		}
		return snowyModel.particleSprite(snowLevel);
	}

	public static void onChunkLoad(ClientLevel level, LevelChunk chunk) {
		ResourceKey<Level> dimension = level.dimension();
		if (!dimension.equals(cachedDimension)) {
			cachedDimension = dimension;
			CHUNK_DATA.clear();
		}
		long chunkKey = chunk.getPos().pack();
		SnowAccumulationData data = ((AttachmentTarget) chunk).getAttached(SnowAccumulation.CHUNK_DATA);
		if (data == null) {
			CHUNK_DATA.remove(chunkKey);
		} else {
			CHUNK_DATA.put(chunkKey, data);
		}
		((AttachmentTarget) chunk).onAttachedSet(SnowAccumulation.CHUNK_DATA).register((oldData, newData) -> {
			if (newData == null) {
				CHUNK_DATA.remove(chunkKey);
			} else {
				CHUNK_DATA.put(chunkKey, newData);
			}
			Minecraft.getInstance().execute(() -> markDirty(oldData, newData));
		});
	}

	public static void onChunkUnload(ClientLevel level, LevelChunk chunk) {
		if (level.dimension().equals(cachedDimension)) {
			CHUNK_DATA.remove(chunk.getPos().pack());
		}
	}

	public static void clear() {
		cachedDimension = null;
		CHUNK_DATA.clear();
	}

	private static void markDirty(@Nullable SnowAccumulationData oldData, @Nullable SnowAccumulationData newData) {
		if (oldData != null) {
			for (Long2ByteMap.Entry entry : oldData.states().long2ByteEntrySet()) {
				long pos = entry.getLongKey();
				if (newData == null || newData.get(pos) != oldData.get(pos)) {
					markDirty(pos);
				}
			}
		}
		if (newData != null) {
			for (Long2ByteMap.Entry entry : newData.states().long2ByteEntrySet()) {
				long pos = entry.getLongKey();
				if (oldData == null || oldData.get(pos) != newData.get(pos)) {
					markDirty(pos);
				}
			}
		}
	}

	private static void markDirty(long pos) {
		BlockPos blockPos = BlockPos.of(pos);
		Minecraft.getInstance().levelRenderer.setSectionDirty(
				SectionPos.blockToSectionCoord(blockPos.getX()),
				SectionPos.blockToSectionCoord(blockPos.getY()),
				SectionPos.blockToSectionCoord(blockPos.getZ()));
	}

	public static void scheduleReload() {
		if (!clientReady) {
			return;
		}
		reloadPending = true;
		reloadRequestedAt = System.currentTimeMillis();
	}

	public static void tick() {
		clientReady = true;
		Set<Block> supportedBlocks = SnowAccumulation.supportedBlocks();
		if (lastSupportedBlocks == null) {
			lastSupportedBlocks = supportedBlocks;
		} else if (lastSupportedBlocks != supportedBlocks) {
			lastSupportedBlocks = supportedBlocks;
			scheduleReload();
		}
		if (!reloadPending || System.currentTimeMillis() - reloadRequestedAt < 400) {
			return;
		}
		reloadPending = false;
		Minecraft.getInstance().reloadResourcePacks();
	}
}
