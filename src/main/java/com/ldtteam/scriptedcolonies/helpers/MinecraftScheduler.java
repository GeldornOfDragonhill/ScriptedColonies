package com.ldtteam.scriptedcolonies.helpers;

import com.ldtteam.scriptedcolonies.runner.ScriptRunnerException;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class MinecraftScheduler {
	public static void schedule(Runnable runnable) {
		ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
		CompletableFuture<Void> future = executor.deferTask(runnable);

		try {
			future.get(15, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new ScriptRunnerException("Waiting for minecraft timed out");
		} catch (Exception exception) {
			throw new ScriptRunnerException("There has been a problem while waiting for minecraft");
		}
	}
}
