package net.teamfruit.clouditem;

import net.minecraftforge.fml.common.event.*;

import net.minecraftforge.fml.common.Mod;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class CloudItem {
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
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
}
