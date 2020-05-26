package com.ldtteam.scriptedcolonies.commands;

import com.ldtteam.scriptedcolonies.runner.ScriptManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.StringTextComponent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class McScriptCommand {

	private static final int requiredPermissionLevel = 4;

	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> mcScriptCommand = Commands.literal("mcscript")
			.requires(commandSource -> commandSource.hasPermissionLevel(McScriptCommand.requiredPermissionLevel))
			.then(Commands.literal("server")
				.then(Commands.literal("run")
					.then(Commands.argument("path", StringArgumentType.string())
						.executes(context -> onRun(context))
					)
				)
			);

		dispatcher.register(mcScriptCommand);
	}

	private static int onRun(CommandContext<CommandSource> context) {
		String pathString = StringArgumentType.getString(context, "path");

		Path path = Paths.get(pathString);

		if(!Files.isRegularFile(path)) {
			WriteBack(context, "Not a valid path");
			return 0;
		}

		try {
			Entity entity = context.getSource().getEntity();

			ScriptManager.run(entity != null ? entity.getUniqueID() : null, path.toString());
		}
		catch(Exception exception) {
			WriteBack(context, "Error running script: " + exception.getMessage());
			return 0;
		}

		return 1;
	}

	private static void WriteBack(CommandContext<CommandSource> context, String message) {
		context.getSource().sendFeedback(new StringTextComponent(message), false);
	}
}
