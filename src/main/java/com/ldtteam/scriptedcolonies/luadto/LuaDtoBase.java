package com.ldtteam.scriptedcolonies.luadto;

import org.squiddev.cobalt.LuaError;

public abstract class LuaDtoBase {
	public abstract void readFromLua(LuaTableReader reader) throws LuaError;
}
