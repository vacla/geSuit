package com.minecraftdimensions.bungeesuitehomes;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Utilities {
	BungeeSuiteHomes plugin;

	public Utilities(BungeeSuiteHomes bsh) {
		plugin = bsh;
	}

	public static final String[] PERMISSION_SETHOME_ALL = {
			"bungeesuite.homes.sethome.*", "bungeesuite.homes.*",
			"bungeesuite.*" };

	public void getMessage(String sender, String message) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("GetServerMessage");
			out.writeUTF(sender);
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);

	}

	public void createBaseTables() {
		if (!plugin.tablesCreated) {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("CreateTable");
				out.writeUTF("BungeeHomes");
				out.writeUTF("CREATE TABLE BungeeHomes (h_id int NOT NULL AUTO_INCREMENT,player VARCHAR(100), home_name VARCHAR(100), server VARCHAR(50), world VARCHAR(50), x double, y double,z double,yaw float, pitch float, PRIMARY KEY (h_id), FOREIGN KEY (player) REFERENCES BungeePlayers (playername))");
			} catch (IOException e) {
				e.printStackTrace();
			}
			new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
					.runTaskAsynchronously(plugin);
			plugin.tablesCreated = true;
			createChatConfig();
		}
	}

	public void createChatConfig() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CreateHomeConfig");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void getGroupList() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("GetHomesGroupList");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskLater(plugin, 40L);
	}

	public void teleportToLocation(String player, String loc) {
		Player p = Bukkit.getPlayer(player);
		String locs[] = loc.split("~");
		World w = Bukkit.getWorld(locs[0]);
		double x = Double.parseDouble(locs[1]);
		double y = Double.parseDouble(locs[2]);
		double z = Double.parseDouble(locs[3]);
		float ya = Float.parseFloat(locs[4]);
		float pi = Float.parseFloat(locs[5]);
		Location location = new Location(w, x, y, z, pi, ya);
		if (p != null) {
			p.teleport(location);
		}else{
			plugin.locqueue.put(player, location);
			return;
		}
	}

	public void importPlayersHomes() {
		String path = "plugins/Essentials/userdata";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".yml");
			}
		});
		int userCount = 0;
		int userHomeCount = 0;
		int homeCount = 0;
		for (File data : listOfFiles) {
			userCount++;
			FileConfiguration user = YamlConfiguration.loadConfiguration(data);
			if (user.contains("homes")) {
				userHomeCount++;
				Set<String> homedata = user.getConfigurationSection("homes")
						.getKeys(false);
				if (homedata != null) {
					for (String homes : homedata) {
						String loc = user.getString("homes."+homes+".world")+"~"+user.getDouble("homes."+homes+".x")+"~"+user.getDouble("homes."+homes+".y")+"~"+user.getDouble("homes."+homes+".z")+"~"+user.getDouble("homes."+homes+".yaw")+"~"+user.getDouble("homes."+homes+".pitch");
						setPlayersHome(data.getName().substring(0, data.getName().length()-4), homes, loc);
						homeCount++;
					}
				}
			}
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD+"Out of "+userCount+ "users, "+userHomeCount+" had homes");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD+"Homes imported: "+homeCount);
	}

	public void setPlayersHome(String player, String homename, String loc) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("SetPlayersHomeImport");
			out.writeUTF(player);
			out.writeUTF(loc);
			out.writeUTF(homename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void setPlayersHome(CommandSender sender, String homename) {

		ArrayList<String> permissions = new ArrayList<String>();
		if (CommandUtil.hasPermission(sender, PERMISSION_SETHOME_ALL)) {
			permissions.add("*");
		} else {
			for (String data : plugin.groups) {
				if (sender.hasPermission("bungeesuite.homes.sethome." + data)) {
					permissions.add(data);
				}
			}
		}
		if (permissions.isEmpty()) {
			getMessage(sender.getName(), "HOME_NO_PERMISSION");
			return;
		}
		Player p = (Player) sender;
		Location l = p.getLocation();
		String loc = l.getWorld().getName() + "~" + l.getX() + "~" + l.getY()
				+ "~" + l.getZ() + "~" + l.getY() + "~" + l.getYaw() + "~"
				+ l.getPitch();
		String perms = "";
		for (String data : permissions) {
			perms += data + "~";
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("SetPlayersHome");
			out.writeUTF(p.getName());
			out.writeUTF(perms);
			out.writeUTF(loc);
			out.writeUTF(homename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void getPlayersHome(Player player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("GetPlayersHome");
			out.writeUTF(player.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
	}

	public void delHome(CommandSender sender, String home) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("DeletePlayersHome");
			out.writeUTF(sender.getName());
			out.writeUTF(home);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);

		if (home.equalsIgnoreCase("home")
				&& plugin.defaultHomes.containsKey(sender.getName())) {
			plugin.defaultHomes.remove(sender.getName());
		}

	}

	public void listPlayersHomes(String name) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("ListPlayersHomes");
			out.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);

	}

	public void sendPlayerHome(CommandSender sender, String homename) {
		Player p = (Player) sender;
		if (homename.equalsIgnoreCase("home")) {
			if (plugin.defaultHomes.containsKey(sender.getName())) {
				Location l = plugin.defaultHomes.get(sender.getName());
				if (!l.getChunk().isLoaded()) {
					l.getChunk().load();
				}
				p.teleport(l);
				this.getMessage(sender.getName(), "SENT_HOME");
				return;
			} else {
				this.getMessage(sender.getName(), "HOME_NOT_EXIST");
				return;
			}
		} else {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(b);
			try {
				out.writeUTF("SendPlayerHome");
				out.writeUTF(sender.getName());
				out.writeUTF(homename);
			} catch (IOException e) {
				e.printStackTrace();
			}
			new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
					.runTaskAsynchronously(plugin);
		}

	}

	public void reloadHomes(CommandSender sender) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("ReloadHomes");
			out.writeUTF(sender.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);
		
	}
}
