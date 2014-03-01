package net.cubespace.geSuitPortals.tasks;

import net.cubespace.geSuitPortals.geSuitPortals;
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
					geSuitPortals.INSTANCE,
					"geSuitPortals",
					bytes.toByteArray());
	
	}

}