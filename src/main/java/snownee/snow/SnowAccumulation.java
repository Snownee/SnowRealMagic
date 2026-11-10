package snownee.snow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import snownee.snow.util.CommonProxy;

/**
 * Server-authoritative snow accumulation on blocks. The state is stored on the chunk so it is saved with the world and
 * synchronized to clients using data attachments.
 */
public final class SnowAccumulation {
	public static final int MAX_STATE = SnowAccumulationData.MAX_STATE;
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(
			NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
			SnowRealMagic.ID);
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<SnowAccumulationData>> CHUNK_DATA = ATTACHMENTS.register(
			"snow_accumulation",
			() -> AttachmentType.builder(() -> SnowAccumulationData.EMPTY)
					.serialize(SnowAccumulationData.CODEC.fieldOf("states"))
					.sync(SnowAccumulationData.STREAM_CODEC)
					.build());
	private static volatile @Nullable Set<Block> supportedBlocksCache;

	/**
	 * Per-level buffer of the snow states changed during the current tick, keyed by packed chunk pos. Changes are only
	 * published to the attachment once per tick, so a burst of updates in one chunk is copied, serialized and sent to
	 * clients a single time.
	 */
	public interface PendingHolder {
		Long2ObjectMap<@Nullable Pending> srm$pendingSnow();
	}

	public record Pending(LevelChunk chunk, Long2ByteOpenHashMap states) {
	}

	private SnowAccumulation() {
	}

	public static void init() {
	}

	public static void register(IEventBus eventBus) {
		ATTACHMENTS.register(eventBus);
	}

	@Nullable
	public static SnowAccumulationData getData(ChunkAccess chunk) {
		return ((IAttachmentHolder) chunk).getExistingDataOrNull(CHUNK_DATA);
	}

	public static Set<Block> supportedBlocks() {
		Set<Block> cache = supportedBlocksCache;
		if (cache != null) {
			return cache;
		}
		synchronized (SnowAccumulation.class) {
			cache = supportedBlocksCache;
			if (cache == null) {
				cache = buildSupportedBlocks();
				supportedBlocksCache = cache;
			}
		}
		return cache;
	}

	public static void invalidateSupportedBlocks() {
		supportedBlocksCache = null;
	}

	private static Set<Block> buildSupportedBlocks() {
		List<Pattern> whitelist = compilePatterns(SnowCommonConfig.supportedBlockWhitelist);
		List<Pattern> blacklist = compilePatterns(SnowCommonConfig.supportedBlockBlacklist);
		Set<Block> blocks = new HashSet<>();
		for (Block block : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			if (matches(whitelist, id) && !matches(blacklist, id)) {
				blocks.add(block);
			}
		}
		return Set.copyOf(blocks);
	}

	private static List<Pattern> compilePatterns(List<String> regexes) {
		List<Pattern> patterns = new ArrayList<>(regexes.size());
		for (String regex : regexes) {
			if (regex.isBlank()) {
				continue;
			}
			try {
				patterns.add(Pattern.compile(regex));
			} catch (PatternSyntaxException e) {
				SnowRealMagic.LOGGER.warn("Invalid block regex '{}' in config", regex, e);
			}
		}
		return patterns;
	}

	private static boolean matches(List<Pattern> patterns, Identifier id) {
		String value = id.toString();
		for (Pattern pattern : patterns) {
			if (pattern.matcher(value).matches()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isSupported(BlockState state) {
		return supportedBlocks().contains(state.getBlock());
	}

	public static int defaultIntensityForLevel(int level) {
		return Math.round(Mth.clamp(level, 0, MAX_STATE) * 100F / MAX_STATE);
	}

	public static int get(ChunkAccess chunk, BlockPos pos) {
		Long2ByteMap states = states(chunk);
		return states == null ? 0 : states.get(pos.asLong());
	}

	public static boolean isEmpty(LevelChunk chunk) {
		Long2ByteMap states = states(chunk);
		return states == null || states.isEmpty();
	}

	private static @Nullable Long2ByteMap states(ChunkAccess chunk) {
		if (chunk instanceof LevelChunk levelChunk && levelChunk.getLevel() instanceof ServerLevel level) {
			Pending pending = ((PendingHolder) level).srm$pendingSnow().get(levelChunk.getPos().pack());
			if (pending != null) {
				return pending.states();
			}
		}
		SnowAccumulationData data = getData(chunk);
		return data == null ? null : data.states();
	}

	public static void set(LevelChunk chunk, BlockPos pos, int state) {
		state = Mth.clamp(state, 0, MAX_STATE);
		if (get(chunk, pos) == state) {
			return;
		}
		Pending pending = pending(chunk);
		if (state <= 0) {
			pending.states().remove(pos.asLong());
		} else {
			pending.states().put(pos.asLong(), (byte) state);
		}
	}

	private static Pending pending(LevelChunk chunk) {
		PendingHolder holder = (PendingHolder) chunk.getLevel();
		Long2ObjectMap<@Nullable Pending> pending = holder.srm$pendingSnow();
		long chunkPos = chunk.getPos().pack();
		Pending entry = pending.get(chunkPos);
		if (entry == null) {
			SnowAccumulationData data = getData(chunk);
			entry = new Pending(chunk, data == null ? new Long2ByteOpenHashMap() : new Long2ByteOpenHashMap(data.states()));
			pending.put(chunkPos, entry);
		}
		return entry;
	}

	/**
	 * Publishes everything buffered during this tick. Called once at the end of the level tick.
	 */
	public static void flushPending(ServerLevel level) {
		Long2ObjectMap<@Nullable Pending> pending = ((PendingHolder) level).srm$pendingSnow();
		if (pending.isEmpty()) {
			return;
		}
		for (Long2ObjectMap.Entry<Pending> entry : pending.long2ObjectEntrySet()) {
			Pending chunkData = entry.getValue();
			Long2ByteOpenHashMap states = chunkData.states();
			LevelChunk chunk = chunkData.chunk();
			IAttachmentHolder holder = chunk;
			if (states.isEmpty()) {
				holder.removeData(CHUNK_DATA);
			} else {
				holder.setData(CHUNK_DATA, SnowAccumulationData.wrap(states));
			}
			chunk.markUnsaved();
		}
		pending.clear();
	}

	public static void increase(LevelChunk chunk, BlockPos pos) {
		set(chunk, pos, get(chunk, pos) + 1);
	}

	public static void clear(LevelChunk chunk, BlockPos pos) {
		set(chunk, pos, 0);
	}

	/**
	 * Blows away all accumulated snow in a sphere around {@code center}, e.g. when a wind charge bursts.
	 */
	public static void clearAround(ServerLevel level, Vec3 center, double radius) {
		int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(center.x - radius));
		int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(center.x + radius));
		int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(center.z - radius));
		int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(center.z + radius));
		int minY = Mth.floor(center.y - radius);
		int maxY = Mth.floor(center.y + radius);
		double radiusSq = radius * radius;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
				if (chunk == null || isEmpty(chunk)) {
					continue;
				}
				ChunkPos chunkPos = chunk.getPos();
				int minX = Math.max(Mth.floor(center.x - radius), chunkPos.getMinBlockX());
				int maxX = Math.min(Mth.floor(center.x + radius), chunkPos.getMaxBlockX());
				int minZ = Math.max(Mth.floor(center.z - radius), chunkPos.getMinBlockZ());
				int maxZ = Math.min(Mth.floor(center.z + radius), chunkPos.getMaxBlockZ());
				for (int x = minX; x <= maxX; x++) {
					for (int y = minY; y <= maxY; y++) {
						for (int z = minZ; z <= maxZ; z++) {
							pos.set(x, y, z);
							if (center.distanceToSqr(x + 0.5D, y + 0.5D, z + 0.5D) > radiusSq) {
								continue;
							}
							if (!isSupported(level.getBlockState(pos))) {
								continue;
							}
							clear(chunk, pos);
						}
					}
				}
			}
		}
	}

	/**
	 * Gives every supported block in a freshly generated chunk its initial snow state based on the biome at the
	 * column's surface. All blocks in the same column share the same state regardless of their height.
	 */
	public static void initializeChunk(LevelChunk chunk) {
		Level level = chunk.getLevel();
		if (level.isClientSide() || !SnowCommonConfig.initialSnowOnLeaves) {
			return;
		}
		if (chunk.hasData(CHUNK_DATA)) {
			return;
		}
		ChunkPos chunkPos = chunk.getPos();
		boolean[] columnsWithBlocks = new boolean[256];
		boolean found = false;
		for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
			LevelChunkSection section = chunk.getSection(sectionIndex);
			if (!section.maybeHas(SnowAccumulation::isSupported)) {
				continue;
			}
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						if (isSupported(section.getBlockState(x, y, z))) {
							columnsWithBlocks[x | z << 4] = true;
							found = true;
						}
					}
				}
			}
		}
		if (!found) {
			return;
		}

		int minY = chunk.getMinY();
		int maxY = chunk.getMaxY();
		int[] columnStates = new int[256];
		for (int z = 0; z < 16; z++) {
			for (int x = 0; x < 16; x++) {
				if (!columnsWithBlocks[x | z << 4]) {
					continue;
				}
				int worldX = chunkPos.getMinBlockX() + x;
				int worldZ = chunkPos.getMinBlockZ() + z;
				int surfaceY = Mth.clamp(
						chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ),
						minY,
						maxY);
				Holder<Biome> biome = chunk.getNoiseBiome(
						QuartPos.fromBlock(worldX),
						QuartPos.fromBlock(surfaceY),
						QuartPos.fromBlock(worldZ));
				columnStates[x | z << 4] = initialSnowState(level, biome, worldX, surfaceY, worldZ);
			}
		}

		Long2ByteOpenHashMap states = new Long2ByteOpenHashMap();
		for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
			LevelChunkSection section = chunk.getSection(sectionIndex);
			if (!section.maybeHas(SnowAccumulation::isSupported)) {
				continue;
			}
			int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						if (!isSupported(section.getBlockState(x, y, z))) {
							continue;
						}
						int state = columnStates[x | z << 4];
						if (state > 0) {
							states.put(BlockPos.asLong(chunkPos.getMinBlockX() + x, baseY + y, chunkPos.getMinBlockZ() + z),
									(byte) state);
						}
					}
				}
			}
		}
		if (!states.isEmpty()) {
			chunk.setData(CHUNK_DATA, SnowAccumulationData.wrap(states));
			chunk.markUnsaved();
		}
	}

	private static int initialSnowState(Level level, Holder<Biome> holder, int x, int y, int z) {
		Biome biome = holder.value();
		if (!biome.hasPrecipitation()) {
			return 0;
		}
		BlockPos surfacePos = new BlockPos(x, y, z);
		if (!SnowCommonConfig.worldGenSnowAndFreezeEverywhere && !CommonProxy.coldEnoughToSnow(level, surfacePos, holder)) {
			return 0;
		}
		return layerForDownfall(Mth.clamp(biome.getModifiedClimateSettings().downfall(), 0F, 1F));
	}

	private static int layerForDownfall(float downfall) {
		List<Number> thresholds = SnowCommonConfig.initialSnowDownfallThresholds;
		int state = 0;
		int limit = Math.min(thresholds.size(), MAX_STATE);
		for (int i = 0; i < limit; i++) {
			Number threshold = thresholds.get(i);
			if (downfall < threshold.floatValue()) {
				break;
			}
			state = i + 1;
		}
		return state;
	}

	/**
	 * @return the current biome's maximum snow state for the column containing {@code pos}
	 */
	public static int maxSnowState(Level level, BlockPos pos) {
		BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
		Holder<Biome> biome = level.getBiome(surfacePos);
		return initialSnowState(level, biome, surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());
	}

}
