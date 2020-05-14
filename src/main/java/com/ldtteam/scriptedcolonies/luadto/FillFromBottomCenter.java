package com.ldtteam.scriptedcolonies.luadto;

import org.squiddev.cobalt.LuaError;

public class FillFromBottomCenter extends LuaDtoBase {
	public BottomCenteredBoxExtends box;
	public String blockType;

	@Override
	public void readFromLua(LuaTableReader reader) throws LuaError {
		this.box = reader.readTable("box", BottomCenteredBoxExtends.class);
		this.blockType = reader.readString("block_type");
	}
}
