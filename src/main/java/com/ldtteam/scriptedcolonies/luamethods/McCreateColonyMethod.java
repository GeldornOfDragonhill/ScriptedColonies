package com.ldtteam.scriptedcolonies.luamethods;

import com.ldtteam.scriptedcolonies.helpers.MinecraftScheduler;
import com.ldtteam.scriptedcolonies.luadto.LuaTableReader;
import com.ldtteam.scriptedcolonies.luadto.Pos;
import com.ldtteam.scriptedcolonies.runner.ScriptRunner;
import com.ldtteam.scriptedcolonies.runner.ScriptRunnerException;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.function.VarArgFunction;

public class McCreateColonyMethod extends VarArgFunction {
	private final ScriptRunner runner;

	public McCreateColonyMethod(ScriptRunner runner) {
		this.runner = runner;
	}

	@Override
	public Varargs invoke(LuaState luaState, Varargs varargs) throws LuaError, UnwindThrowable {
		if(varargs.count() != 1) {
			return Constants.NONE;
		}

		Pos args = LuaTableReader.readTable(varargs.arg(1).checkTable(), Pos.class);

		MinecraftScheduler.schedule(() -> {
			IColonyManager colonyManager = IColonyManager.getInstance();


			MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);

			ServerPlayerEntity serverPlayerEntity = server.getPlayerList().getPlayerByUUID(this.runner.getUserId());

			if(serverPlayerEntity == null) {
				throw new ScriptRunnerException("Couldn't load user");
			}

			//TODO: make this configurable
			ServerWorld world = server.getWorld(DimensionType.OVERWORLD);

			IColony colony = colonyManager.getIColonyByOwner(world, serverPlayerEntity);

			if(colony != null) {
				colonyManager.deleteColonyByWorld(colony.getID(), true, world);
				serverPlayerEntity.sendMessage(new StringTextComponent("Deleted existing colony"));
			}

			colonyManager.createColony(world, args.toBlockPos(), serverPlayerEntity, com.minecolonies.api.util.constant.Constants.DEFAULT_STYLE);
			serverPlayerEntity.sendMessage(new StringTextComponent("Created colony"));
		});

		return Constants.NONE;
	}


}
