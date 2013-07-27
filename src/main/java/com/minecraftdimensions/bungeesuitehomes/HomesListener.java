package com.minecraftdimensions.bungeesuitehomes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class HomesListener implements PluginMessageListener, Listener {

	BungeeSuiteHomes plugin;
	
	private static final String[] PERMISSION_NODES = { "bungeesuite.homes.death", "bungeesuite.homes.*", "bungeesuite.*" };
	
	public HomesListener(BungeeSuiteHomes bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e){
		if(CommandUtil.hasPermission(e.getPlayer(), PERMISSION_NODES)){
			if(plugin.defaultHomes.containsKey(e.getPlayer())){
				Location loc = plugin.defaultHomes.get(e.getPlayer());
				if(loc==null){
					e.setRespawnLocation(e.getPlayer().getWorld().getSpawnLocation());
					return;
				}
			e.setRespawnLocation(plugin.defaultHomes.get(e.getPlayer()));
			plugin.utils.getMessage(e.getPlayer().getName(), "SENT_HOME");
			}else{
				plugin.utils.getMessage(e.getPlayer().getName(), "HOME_NOT_SET");
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if(!plugin.tablesCreated){
			plugin.utils.createBaseTables();
			plugin.utils.createChatConfig();
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("GetPlayersHome");
				out.writeUTF(e.getPlayer().getName());
			} catch (IOException es) {
				es.printStackTrace();
			}
			new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
					.runTaskLater(plugin, 40L);
			plugin.utils.getGroupList();
		}else{
		plugin.utils.getPlayersHome(e.getPlayer());
		}
	}
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e){
		if(plugin.defaultHomes.containsKey(e.getPlayer())){
			plugin.defaultHomes.remove(e.getPlayer());
		}
	}

	@Override
	public void onPluginMessageReceived(String pluginChannel, Player reciever,
			byte[] message) {
		if (!pluginChannel
				.equalsIgnoreCase(BungeeSuiteHomes.INCOMING_PLUGIN_CHANNEL))
			return;

		DataInputStream in = new DataInputStream(new ByteArrayInputStream(
				message));

		String channel = null;
		try {
			channel = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (channel.equalsIgnoreCase("ReceiveGroups")) {
			String groups = null;
			try {
				groups = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for(String data: groups.split("~")){
				System.out.println(data);
				plugin.groups.add(data);
			}
			return;
		}
		String player = null;
		try {
			player = in.readUTF();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (channel.equalsIgnoreCase("TeleportPlayerToHome")) {
			String loc = null;
			try {
				loc = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			plugin.utils.teleportToLocation(player, loc);
			return;
		}
		if (channel.equalsIgnoreCase("ReceiveHome")) {
			String loc = null;
			try {
				loc = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String locs[] = loc.split("~");
			World w = Bukkit.getWorld(locs[0]);
			double x = Double.parseDouble(locs[1]);
			double y = Double.parseDouble(locs[2]);
			double z = Double.parseDouble(locs[3]);
			float ya = Float.parseFloat(locs[4]);
			float pi = Float.parseFloat(locs[5]);
			Location location = new Location(w, x, y, z, pi,ya);
			plugin.defaultHomes.put(Bukkit.getPlayer(player), location);
			return;
		}
	}

}
