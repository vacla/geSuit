package net.cubespace.geSuiteSpawn.tasks;

import net.cubespace.geSuiteSpawn.geSuitSpawn;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;


public class PluginMessageTask extends BukkitRunnable {
    
	private final ByteArrayOutputStream bytes;

	public PluginMessageTask(ByteArrayOutputStream bytes) {
		this.bytes = bytes;
	}
	
	public void run() {
			Bukkit.getOnlinePlayers()[0].sendPluginMessage(
					geSuitSpawn.INSTANCE,
					"geSuitSpawns",
					bytes.toByteArray());
	
	}

}