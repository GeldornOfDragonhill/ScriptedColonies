package com.ldtteam.scriptedcolonies.helpers;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.util.ArrayList;
import java.util.List;

public class CachedBlockPlacer {
	private static final int maxSizeBeforeFlush = 8192;

	private DimensionType dimensionType;
	private List<Tuple<BlockPos, Block>> blockCache = new ArrayList<>();

	public CachedBlockPlacer(ServerWorld world) {
		this.dimensionType = world.getDimension().getType();
	}

	public void setBlock(BlockPos pos, Block block) {
		this.blockCache.add(new Tuple<>(pos, block));

		if(this.blockCache.size() >= maxSizeBeforeFlush) {
			this.flush();
		}
	}

	public void flush() {
		if(this.blockCache.isEmpty()) {
			return;
		}

		MinecraftScheduler.schedule(() -> {
			MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
			ServerWorld world = server.getWorld(this.dimensionType);
			for(Tuple<BlockPos, Block> entry : this.blockCache) {
				world.setBlockState(entry.getA(), entry.getB().getDefaultState());
			}
		});

		Thread.yield();

		this.blockCache.clear();
	}
}
