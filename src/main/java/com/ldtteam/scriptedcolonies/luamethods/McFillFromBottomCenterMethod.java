package com.ldtteam.scriptedcolonies.luamethods;

import com.ldtteam.scriptedcolonies.helpers.CachedBlockPlacer;
import com.ldtteam.scriptedcolonies.luadto.FillFromBottomCenter;
import com.ldtteam.scriptedcolonies.luadto.LuaTableReader;
import com.ldtteam.scriptedcolonies.runner.ScriptRunner;
import com.ldtteam.scriptedcolonies.runner.ScriptRunnerException;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.function.VarArgFunction;

public class McFillFromBottomCenterMethod extends VarArgFunction {
	private final ScriptRunner runner;

	public McFillFromBottomCenterMethod(ScriptRunner runner) {
		this.runner = runner;
	}

	@Override
	public Varargs invoke(LuaState luaState, Varargs varargs) throws LuaError, UnwindThrowable {
		if(varargs.count() != 1) {
			return Constants.NONE;
		}

		FillFromBottomCenter args = LuaTableReader.readTable(varargs.arg(1).checkTable(), FillFromBottomCenter.class);

		MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);

		Block block;
		if(args.blockType.equals("minecraft:air")) {
			block = Blocks.AIR;
		} else {
			block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(args.blockType));
			if(block == Blocks.AIR) {
				throw new ScriptRunnerException("Unknown block: " + args.blockType);
			}
		}

		//TODO: make this configurable
		ServerWorld world = server.getWorld(DimensionType.OVERWORLD);

		CachedBlockPlacer placer = new CachedBlockPlacer(world);

		for(int layer = 0; layer < args.box.height; ++layer) {
			int y = args.box.pos.y + layer;
			for(int x = args.box.pos.x - args.box.radius; x < args.box.pos.x + args.box.radius; ++x) {
				for(int z = args.box.pos.z - args.box.radius; z < args.box.pos.z + args.box.radius; ++z) {
					placer.setBlock(new BlockPos(x, y, z), block);
				}
			}
		}

		placer.flush();

		return Constants.NONE;
	}
}
