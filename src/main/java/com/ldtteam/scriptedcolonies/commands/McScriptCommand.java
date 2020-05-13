package com.ldtteam.scriptedcolonies.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

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
		String path = StringArgumentType.getString(context, "path");
		WriteBack(context, "server run [N/A]: " + path);
		return 1;
	}

	private static void WriteBack(CommandContext<CommandSource> context, String message) {
		context.getSource().sendFeedback(new StringTextComponent(message), false);
	}
}
