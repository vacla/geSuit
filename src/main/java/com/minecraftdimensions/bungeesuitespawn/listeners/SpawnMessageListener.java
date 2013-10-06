package com.minecraftdimensions.bungeesuitespawn.listeners;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.minecraftdimensions.bungeesuitespawn.managers.SpawnManager;

public class SpawnMessageListener implements PluginMessageListener, Listener {

	@Override
	public void onPluginMessageReceived(String channel, Player player,
			byte[] message) {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));
		String task = null;

		try {
			task = in.readUTF();
			if(task.equals("SendSpawn")){
				SpawnManager.addSpawn(in.readUTF(),in.readUTF(),in.readDouble(),in.readDouble(),in.readDouble(),in.readFloat(),in.readFloat());
			}else if ( task.equals("TeleportToLocation")){
				SpawnManager.teleportPlayer(in.readUTF(), in.readUTF(), in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat());
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	

}
