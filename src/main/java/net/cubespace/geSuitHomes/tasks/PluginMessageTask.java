package net.cubespace.geSuitHomes.tasks;

import net.cubespace.geSuitHomes.geSuitHomes;
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
					geSuitHomes.instance,
					"geSuitHomes",
					bytes.toByteArray());
	
	}

}