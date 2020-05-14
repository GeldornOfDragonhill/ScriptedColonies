package com.ldtteam.scriptedcolonies.luadto;

import net.minecraft.util.math.BlockPos;
import org.squiddev.cobalt.LuaError;

public class Pos extends LuaDtoBase {
	public int x;
	public int y;
	public int z;

	@Override
	public void readFromLua(LuaTableReader reader) throws LuaError {
		this.x = reader.readInt("x");
		this.y = reader.readInt("y");
		this.z = reader.readInt("z");
	}

	public BlockPos toBlockPos() {
		return new BlockPos(x, y, z);
	}
}
