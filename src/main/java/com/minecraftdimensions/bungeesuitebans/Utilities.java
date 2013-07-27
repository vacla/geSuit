package com.minecraftdimensions.bungeesuitebans;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;


public class Utilities {
	BungeeSuiteBans plugin;

	public Utilities(BungeeSuiteBans bst) {
		plugin = bst;
	}
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
				out.writeUTF("BungeeBans");
				out.writeUTF("CREATE TABLE BungeeBans (player VARCHAR(100), banned_by VARCHAR(100), reason VARCHAR(200), type VARCHAR(20), banned_on DATETIME, banned_until DATETIME, FOREIGN KEY (player) REFERENCES BungeePlayers (playername), FOREIGN KEY (banned_by) REFERENCES BungeePlayers (playername))");
			} catch (IOException e) {
				e.printStackTrace();
			}
			new PluginMessageTask(this.plugin,
					Bukkit.getOnlinePlayers()[0], b)
					.runTaskAsynchronously(plugin);
			plugin.tablesCreated = true;
		}
	}

	public void banPlayer(String sender, String player, String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("BanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}

	public void kickAll(String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("KickAll");
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}

	public void kickPlayer(String sender, String player, String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("KickPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}

	public void tempBanPlayer(String sender, String player, String timing) {
		String [] arg1 = timing.split(" "); 
		int minuteIncrease = 0;
		int hourIncrease = 0;
		int dateIncrease = 0;
		String message = "";
		for (int i = 1; i < arg1.length; i++) {
			try {
				if(arg1[i].length()>1){
				if (arg1[i].substring(0, 2).equalsIgnoreCase("d:")) {
					dateIncrease += Integer.parseInt(arg1[i].substring(
							2, arg1[i].length()));
				}else
				if (arg1[i].substring(0, 2).equalsIgnoreCase("h:")) {
					hourIncrease += Integer.parseInt(arg1[i].substring(
							2, arg1[i].length()));
				}else
				if (arg1[i].substring(0, 2).equalsIgnoreCase("m:")) {
					minuteIncrease += Integer.parseInt(arg1[i]
							.substring(2, arg1[i].length()));
				}else{
					message+=arg1[i]+" ";
				}
				}else{
					message+=arg1[i]+" ";
				}
			} catch (NumberFormatException e) {
				Bukkit.getPlayer(sender).sendMessage(ChatColor.RED
						+ "An incorrect value was used for the time. /tempban (playername) (d:days h:hours, m: minutes)");
				return;
			}
		}
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("TempBanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
			out.writeInt(minuteIncrease);
			out.writeInt(hourIncrease);
			out.writeInt(dateIncrease);
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
	}

	public void unbanPlayer(String sender, String player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("UnbanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}
	public void createBansConfig() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CreateBansConfig");
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);	
	}
	public void reloadBans(String string) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("ReloadBans");
			out.writeUTF(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin, Bukkit.getOnlinePlayers()[0], b)
				.runTaskAsynchronously(plugin);	
	}
	public void ipBanPlayer(String sender, String player, String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("IPBanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}
	public void unipBanPlayer(String sender, String player, String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("UnIPBanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}
	public void checkPlayerBans(String sender, String player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CheckPlayerBans");
			out.writeUTF(sender);
			out.writeUTF(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(this.plugin,
				Bukkit.getOnlinePlayers()[0], b).runTaskAsynchronously(plugin);
		
	}

	
	

}
