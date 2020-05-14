package com.ldtteam.scriptedcolonies.luadto;

import org.squiddev.cobalt.LuaError;

public class BottomCenteredBoxExtends extends LuaDtoBase {
	public Pos pos;
	public int radius;
	public int height;

	@Override
	public void readFromLua(LuaTableReader reader) throws LuaError {
		this.pos = reader.readTable("pos", Pos.class);
		this.radius = reader.readInt("radius");
		this.height = reader.readInt("height");
	}
}
