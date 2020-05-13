package com.ldtteam.scriptedcolonies.runner;

import org.apache.logging.log4j.Level;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.compiler.CompileException;
import org.squiddev.cobalt.compiler.LoadState;
import org.squiddev.cobalt.function.LuaFunction;
import org.squiddev.cobalt.function.VarArgFunction;
import org.squiddev.cobalt.lib.BaseLib;
import org.squiddev.cobalt.lib.platform.VoidResourceManipulator;

import java.io.InputStream;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class ScriptRunner {
	private BiConsumer<String, Level> messageAction;
	private LuaState state;
	private LuaTable globals;

	private final static HashSet<String> allowedGlobals = new HashSet<>();

	static {
		allowedGlobals.add("pairs");
	}

	public ScriptRunner(BiConsumer<String, Level> messageAction) {
		this.messageAction = messageAction;

		this.state = LuaState.builder()
			.resourceManipulator(new VoidResourceManipulator())
			.debug(new ScriptDebugHandler())
			.build();

		this.setupGlobals();

		state.setupThread(globals);
	}

	private void setupGlobals() {
		this.globals = new LuaTable();
		this.globals.load(state, new BaseLib());

		//Whitelist globals
		try {
			LuaValue[] globalKeys = this.globals.keys();

			for(int i = globalKeys.length - 1; i >= 0; --i) {
				LuaValue globalKey = globalKeys[i];
				if((globalKey instanceof LuaString)) {
					String globalName = globalKey.toString();

					if(allowedGlobals.contains(globalName)) {
						continue;
					}
				}

				this.globals.rawset(globalKey, Constants.NIL);
			}
		} catch (LuaError luaError) {
			throw new RuntimeException("Couldn't sanitize lua globals");
		}

		//Add custom methods
		globals.rawset("mcprint", new McPrintMethod(this));
	}

	private class McPrintMethod extends VarArgFunction {

		private final ScriptRunner runner;

		McPrintMethod(ScriptRunner runner) {
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

			this.runner.messageAction.accept(arg, Level.OFF);

			return Constants.NONE;
		}
	}

	public void run(InputStream program) {
		try {
			LuaFunction function = LoadState.load(this.state, program, "test", this.globals);
			LuaValue retval = function.call(this.state);

			if(retval instanceof LuaInteger) {
				int intRetval = ((LuaInteger)retval).v;
				if(intRetval != 0) {
					this.messageAction.accept("Script error return code: " + intRetval, Level.INFO);
				}
			}

		} catch (UnwindThrowable throwable) {
			this.messageAction.accept("Script unwind error: " + throwable.getMessage(), Level.INFO);
		} catch (CompileException exception) {
			this.messageAction.accept("Script compiler error: " + exception.getMessage(), Level.INFO);
		} catch(Exception exception) {
			this.messageAction.accept("Script error: " + exception.getMessage(), Level.INFO);
		}
	}
}
