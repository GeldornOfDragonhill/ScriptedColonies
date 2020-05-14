package com.ldtteam.scriptedcolonies.runner;

import com.ldtteam.scriptedcolonies.luamethods.McCreateColonyMethod;
import com.ldtteam.scriptedcolonies.luamethods.McFillFromBottomCenterMethod;
import com.ldtteam.scriptedcolonies.luamethods.McPrintMethod;
import org.apache.logging.log4j.Level;
import org.squiddev.cobalt.*;
import org.squiddev.cobalt.compiler.CompileException;
import org.squiddev.cobalt.compiler.LoadState;
import org.squiddev.cobalt.function.LuaFunction;
import org.squiddev.cobalt.lib.BaseLib;
import org.squiddev.cobalt.lib.platform.VoidResourceManipulator;

import java.io.InputStream;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ScriptRunner {
	private BiConsumer<String, Level> messageAction;
	private UUID userId;
	private LuaState state;
	private LuaTable globals;

	private final static HashSet<String> allowedGlobals = new HashSet<>();

	static {
		allowedGlobals.add("pairs");
	}

	public BiConsumer<String, Level> getMessageAction() {
		return this.messageAction;
	}

	public UUID getUserId() {
		return this.userId;
	}

	public ScriptRunner(BiConsumer<String, Level> messageAction, UUID userId) {
		this.messageAction = messageAction;
		this.userId = userId;

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
		globals.rawset("mc_print", new McPrintMethod(this));
		globals.rawset("mc_fill_from_bottom_center", new McFillFromBottomCenterMethod(this));
		globals.rawset("mc_create_colony", new McCreateColonyMethod(this));
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

			this.messageAction.accept("Script done", Level.INFO);

		} catch (UnwindThrowable throwable) {
			this.messageAction.accept("Script unwind error: " + throwable.getMessage(), Level.INFO);
		} catch (CompileException exception) {
			this.messageAction.accept("Script compiler error: " + exception.getMessage(), Level.INFO);
		} catch(Exception exception) {
			this.messageAction.accept("Script error: " + exception.getMessage(), Level.INFO);
		}
	}
}
