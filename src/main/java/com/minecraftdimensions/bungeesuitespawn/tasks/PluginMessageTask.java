package com.minecraftdimensions.bungeesuitespawn.tasks;

import java.io.ByteArrayOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import com.minecraftdimensions.bungeesuitespawn.BungeeSuiteSpawn;


public class PluginMessageTask extends BukkitRunnable {
    
	private final ByteArrayOutputStream bytes;

	public PluginMessageTask(ByteArrayOutputStream bytes) {
		this.bytes = bytes;
	}
	
	public void run() {
			Bukkit.getOnlinePlayers()[0].sendPluginMessage(
					BungeeSuiteSpawn.INSTANCE,
					BungeeSuiteSpawn.OUTGOING_PLUGIN_CHANNEL,
					bytes.toByteArray());
	
	}

}