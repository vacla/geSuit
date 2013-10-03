package com.minecraftdimensions.bungeesuiteportals.listeners;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.minecraftdimensions.bungeesuiteportals.managers.PortalsManager;

public class PortalsMessageListener implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String channel, Player player,
			byte[] message) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));
		String task = null;

		try {
			task = in.readUTF();
			if(task.equals("TeleportPlayer")){
				Player p = Bukkit.getPlayer(in.readUTF());
				Location l =new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat());
				if(p==null){
					PortalsManager.pendingTeleports.put(p, l);
				}else{
					p.teleport(l);
				}
			}
			else if (task.equals("SendPortal")) {
				PortalsManager.addPortal(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF(), new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble()), new Location(Bukkit.getWorld(in.readUTF()), in.readDouble(), in.readDouble(), in.readDouble()));
			}else if(task.equals("DeletePortal")){
				PortalsManager.removePortal(in.readUTF());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
