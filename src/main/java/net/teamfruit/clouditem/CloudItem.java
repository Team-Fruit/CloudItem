package net.teamfruit.clouditem;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.*;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, acceptableRemoteVersions = "*")
public class CloudItem {
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		Log.log.info("Starting CloudItem...");
		event.registerServerCommand(new ModCommand());
	}

	@Mod.EventHandler
	public void onServerStarted(FMLServerStartedEvent event) {
	}

	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			ServerThreadExecutor.INSTANCE.executeQueuedTaskImmediately();
		}
	}
}
