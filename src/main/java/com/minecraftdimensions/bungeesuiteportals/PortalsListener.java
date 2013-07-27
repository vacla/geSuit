package com.minecraftdimensions.bungeesuiteportals;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PortalsListener implements PluginMessageListener, Listener {

	BungeeSuitePortals plugin;

	public PortalsListener(BungeeSuitePortals bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if(!plugin.tablesCreated){
			plugin.utils.createBaseTables();
		}
		if(!plugin.havePortals){
			plugin.utils.getPortals();
		}
	}

	@Override
	public void onPluginMessageReceived(String pluginChannel, Player reciever,
			byte[] message) {
		if (!pluginChannel.equalsIgnoreCase(BungeeSuitePortals.INCOMING_PLUGIN_CHANNEL))
			return;
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));

		String channel = null;
		try {
			channel = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(channel.equalsIgnoreCase("Portal")){
			try {
				String name = in.readUTF();
				String type = in.readUTF();
				String dest = in.readUTF();
				String world = in.readUTF();
				String filltype = in.readUTF();
				int xmax = in.readInt();
				int xmin = in.readInt();
				int ymax = in.readInt();
				int ymin = in.readInt();
				int zmax = in.readInt();
				int zmin = in.readInt();
				plugin.utils.getPortal(name, type, dest, world, filltype, xmax, xmin, ymax, ymin, zmax, zmin);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(channel.equalsIgnoreCase("PortalDelete")){
			try {
				String name = in.readUTF();
				plugin.utils.removePortal(name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
