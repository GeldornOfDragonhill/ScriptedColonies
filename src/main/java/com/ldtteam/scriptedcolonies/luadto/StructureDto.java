package com.ldtteam.scriptedcolonies.luadto;

import org.squiddev.cobalt.LuaError;

public class StructureDto extends LuaDtoBase {
	public String name;
	public Pos pos;
	public int rotation;
	public boolean mirrored;

	@Override
	public void readFromLua(LuaTableReader reader) throws LuaError {
		this.name = reader.readString("name");
		this.pos = reader.readTable("pos", Pos.class);
		this.rotation = reader.readInt("rotation");
		this.mirrored = reader.readBool("mirrored");
	}
}
