package snownee.snow.block.entity;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import snownee.kiwi.block.entity.ModBlockEntity;
import snownee.snow.CoreModule;
import snownee.snow.block.SnowVariant;

public class SnowBlockEntity extends ModBlockEntity implements RenderDataBlockEntity {

	public static class Options {
		public boolean renderOverlay;
	}

	private @Nullable RenderData renderData;
	public Options options = new Options();
	protected BlockState containedState = Blocks.AIR.defaultBlockState();

	public SnowBlockEntity(BlockPos pos, BlockState containedState) {
		this(CoreModule.TILE.get(), pos, containedState);
	}

	public SnowBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public BlockState getContainedState() {
		return containedState;
	}

	public void setContainedState(BlockState state) {
		setContainedState(state, true);
	}

	public boolean setContainedState(BlockState state, boolean update) {
		if (this.containedState == state || state.getBlock() instanceof SnowVariant) {
			return false;
		}
		this.containedState = state;
		if (level != null) {
			if (update) {
				if (level.isClientSide()) {
					level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 11);
				} else {
					refresh();
				}
			}
		}
		return true;
	}

	public void loadContainedState(ValueInput data, boolean network) {
		boolean renderOverlay = data.getBooleanOr("RO", false);
		boolean changed = options.renderOverlay != renderOverlay;
		options.renderOverlay = renderOverlay;
		Optional<BlockState> blockState = parseContainedState(data);
		if (blockState.isPresent()) {
			changed |= setContainedState(blockState.get(), network);
		}
		if (changed && network) {
			refresh();
		}
	}

	public static Optional<BlockState> parseContainedState(ValueInput input) {
		if (input.keySet().contains("Block")) {
			return input.read("Block", Identifier.CODEC).map(BuiltInRegistries.BLOCK::getValue).map(Block::defaultBlockState);
		} else {
			return input.read("State", BlockState.CODEC);
		}
	}

	public void saveContainedState(ValueOutput output, boolean network) {
		if (getContainedState() == getContainedState().getBlock().defaultBlockState()) {
			output.store("Block", Identifier.CODEC, BuiltInRegistries.BLOCK.getKey(getContainedState().getBlock()));
		} else {
			output.store("State", BlockState.CODEC, getContainedState());
		}
		if (options.renderOverlay) {
			output.putBoolean("RO", true);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		loadContainedState(input, false);
	}

	@Override
	protected void readPacketData(ValueInput valueInput) {
		loadContainedState(valueInput, true);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		saveContainedState(output, false);
	}

	@Override
	protected void writePacketData(ValueOutput valueOutput) {
		saveContainedState(valueOutput, true);
	}

	@Override
	public RenderData getRenderData() {
		if (renderData == null || renderData.camo() != containedState) {
			renderData = new RenderData(containedState, options);
		}
		return renderData;
	}
}
