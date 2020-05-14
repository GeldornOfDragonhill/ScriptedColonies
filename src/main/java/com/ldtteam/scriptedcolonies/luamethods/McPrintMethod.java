package com.ldtteam.scriptedcolonies.luamethods;

import com.ldtteam.scriptedcolonies.runner.ScriptRunner;
import org.apache.logging.log4j.Level;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.function.VarArgFunction;

public class McPrintMethod extends VarArgFunction {

	private final ScriptRunner runner;

	public McPrintMethod(ScriptRunner runner) {
		this.runner = runner;
	}

	@Override
	public Varargs invoke(LuaState luaState, Varargs varargs) throws LuaError, UnwindThrowable {
		if(varargs.count() != 1) {
			return Constants.NONE;
		}

		LuaValue arg0 = varargs.arg(1);

		if(!(arg0 instanceof LuaString)) {
			return Constants.NONE;
		}

		String arg = ((LuaString)arg0).toString();

		this.runner.getMessageAction().accept(arg, Level.OFF);

		return Constants.NONE;
	}
}
