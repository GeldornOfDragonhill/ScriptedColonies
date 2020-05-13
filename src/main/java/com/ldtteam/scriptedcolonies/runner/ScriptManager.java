package com.ldtteam.scriptedcolonies.runner;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class ScriptManager {

	private static Logger logger = LogManager.getLogger(Constants.MOD_ID);
	private final static Lock lock = new ReentrantLock();

	private static class ScriptInstance {

		public ScriptInstance(UUID playerId, Consumer<BiConsumer<String, Level>> thread) {
			this.playerId = playerId;
			this.thread = new Thread(() -> thread.accept(this::message));
			this.thread.start();
		}

		private Thread thread;
		private UUID playerId;

		public boolean isAlive() {
			return this.thread.isAlive();
		}

		public void message(String message, Level logLevel) {
			if(playerId != null) {
				ThreadTaskExecutor<?> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
				CompletableFuture<Void> future = executor.deferTask(() -> {
					MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
					ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(playerId);
					player.sendMessage(new StringTextComponent(message));
				});

				try {
					future.get(5, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					throw new ScriptRunnerException("Waiting for minecraft timed out");
				} catch (Exception exception) {
					throw new ScriptRunnerException("There has been a problem while waiting for minecraft");
				}

			} else {
				logLevel = Level.INFO;
			}

			if(logLevel != Level.OFF) {
				logger.log(logLevel, message);
			}
		}
	}

	private static ScriptInstance scriptInstance = null;

	public static void run(UUID userId, String path) throws FileNotFoundException {

		FileInputStream stream = new FileInputStream(path);

		logger.info("Starting mcscript: " + path);

		ScriptManager.run(userId, stream);
	}

	public static void run(UUID userId, InputStream stream) {
		lock.lock();
		try {
			if(scriptInstance != null) {
				if(scriptInstance.isAlive()) {
					throw new ScriptRunnerException("Another script is currently running");
				}
				scriptInstance = null;
			}

			scriptInstance = new ScriptInstance(userId, messageAction -> runThread(stream, messageAction));
		}
		finally {
			lock.unlock();
		}
	}

	private static void runThread(InputStream stream, BiConsumer<String, Level> messageAction) {
		try {
			ScriptRunner runner = new ScriptRunner(messageAction);
			runner.run(stream);
		}
		catch(Exception exception) {
			messageAction.accept("There has been an error running a script: " + exception.getMessage(), Level.ERROR);
		}
	}
}
