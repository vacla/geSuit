package com.minecraftdimensions.bungeesuitewarps;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class WarpsListener implements PluginMessageListener, Listener {

	BungeeSuiteWarps plugin;

	public WarpsListener(BungeeSuiteWarps bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if(!plugin.tablesCreated){
			plugin.utils.createBaseTables();
		}
		if(plugin.pendingWarps.containsKey(e.getPlayer().getName())){
			e.getPlayer().teleport(plugin.pendingWarps.get(e.getPlayer().getName()));
			plugin.pendingWarps.remove(e.getPlayer().getName());
		}
	}

	@Override
	public void onPluginMessageReceived(String pluginChannel, Player reciever,
			byte[] message) {
		if (!pluginChannel
				.equalsIgnoreCase(BungeeSuiteWarps.INCOMING_PLUGIN_CHANNEL))
			return;

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));

		String channel = null;
		try {
			channel = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String player = null;
		try {
			player = in.readUTF();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (channel.equalsIgnoreCase("WarpPlayer")) {

			String loc = null;
			try {
				loc = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			plugin.utils.warpPlayer(player,loc);
		}

	}

}
