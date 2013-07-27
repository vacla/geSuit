package com.minecraftdimensions.bungeesuitebans;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

public class BansListener implements PluginMessageListener, Listener {

	BungeeSuiteBans plugin;

	public BansListener(BungeeSuiteBans bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if (!plugin.tablesCreated) {
			plugin.utils.createBaseTables();
			plugin.utils.createBansConfig();
		}
	}

	@Override
	public void onPluginMessageReceived(String pluginChannel, Player reciever,
			byte[] message) {
		if (!pluginChannel
				.equalsIgnoreCase(BungeeSuiteBans.INCOMING_PLUGIN_CHANNEL))
			return;
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));

		String channel = null;
		try {
			channel = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (channel.equalsIgnoreCase("AltAccount")) {
			String msg = null;
			try {
				msg = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			new ATask(msg).runTaskAsynchronously(plugin);
			return;
		}
	}
}

class ATask extends BukkitRunnable {
	private static final String[] PERMISSION_NODES = {
			"bungeesuite.ban.sameip", "bungeesuite.ban.*",
			"bungeesuite.admin", "bungeesuite.*" };

	String message = null;

	public ATask(String msg) {
		this.message = msg;
	}

	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (CommandUtil.hasPermission(player, PERMISSION_NODES)) {
				player.sendMessage(message);
			}
		}
	}

}