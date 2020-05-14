package com.ldtteam.scriptedcolonies.luadto;

import com.ldtteam.scriptedcolonies.runner.ScriptRunnerException;
import org.squiddev.cobalt.LuaError;
import org.squiddev.cobalt.LuaTable;
import org.squiddev.cobalt.LuaValue;

public class LuaTableReader {
	private final LuaTable table;

	public LuaTableReader(LuaValue value) throws LuaError {
		this.table = value.checkTable();
	}

	public LuaTableReader(LuaTable value) {
		this.table = value;
	}

	public int readInt(String key) throws LuaError {
		return table.rawget(key).checkInteger();
	}

	public String readString(String key) throws LuaError {
		return table.rawget(key).checkString();
	}

	public <T extends  LuaDtoBase> T readTable(String key, Class<T> type) throws LuaError {
		return readTable(table.rawget(key).checkTable(), type);
	}

	public static <T extends  LuaDtoBase> T readTable(LuaTable table, Class<T> type) throws LuaError {

		T instance;

		try {
			instance = type.newInstance();
		} catch (Exception exception) {
			throw new ScriptRunnerException("Could not create instance of type " + type.getName());
		}

		instance.readFromLua(new LuaTableReader(table));

		return instance;
	}
}
