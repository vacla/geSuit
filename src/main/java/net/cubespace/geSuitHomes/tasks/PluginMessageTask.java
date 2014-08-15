package net.cubespace.geSuitHomes.tasks;

import net.cubespace.geSuitHomes.geSuitHomes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;

public class PluginMessageTask extends BukkitRunnable {

	private final ByteArrayOutputStream bytes;

	public PluginMessageTask(ByteArrayOutputStream bytes) {
		this.bytes = bytes;
	}
	
	public void run() {
		Player player = Bukkit.getOnlinePlayers().iterator().next();
		if (player != null) {
			player.sendPluginMessage(
					geSuitHomes.instance,
					"geSuitHomes",
					bytes.toByteArray());
		} else {
			System.out.println(ChatColor.RED + "Unable to send Plugin Message - No players online.");
		}
	}

}