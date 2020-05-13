package com.ldtteam.scriptedcolonies;

import com.ldtteam.scriptedcolonies.commands.McScriptCommand;
import com.ldtteam.scriptedcolonies.config.Constants;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod(Constants.modId)
public class ScriptedColonies {

	public ScriptedColonies() {
		Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(ScriptedColonies.class);
	}

	@SubscribeEvent
	public static void onServerStarting(FMLServerStartingEvent event) {
		CommandDispatcher<CommandSource> commandDispatcher = event.getCommandDispatcher();
		McScriptCommand.register(commandDispatcher);
	}
}
