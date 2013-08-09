package com.minecraftdimensions.bungeesuitebans.managers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import com.minecraftdimensions.bungeesuitebans.BungeeSuiteBans;
import com.minecraftdimensions.bungeesuitebans.tasks.PluginMessageTask;




public class BansManager {
	
	public static void banPlayer(String sender, String player, String msg) {
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
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);	
	}

	public static void kickAll(String sender,String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("KickAll");
			out.writeUTF(sender);
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
		
	}

	public static void kickPlayer(String sender, String player, String msg) {
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
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
		
	}

	public static void tempBanPlayer(String sender, String player, String timing, Command command) {
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
				Bukkit.getPlayer(sender).sendMessage(command.getUsage());
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
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
	}

	public static void unbanPlayer(String sender, String player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("UnbanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
		
	}

	public static void reloadBans(String string) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("ReloadBans");
			out.writeUTF(string);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(b)
				.runTaskAsynchronously(BungeeSuiteBans.instance);	
	}
	
	public static void ipBanPlayer(String sender, String player, String msg) {
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
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
		
	}
	
	public static void unipBanPlayer(String sender, String player, String msg) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("UnIPBanPlayer");
			out.writeUTF(sender);
			out.writeUTF(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
		
	}
	
	public static void checkPlayerBans(String sender, String player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("CheckPlayerBans");
			out.writeUTF(sender);
			out.writeUTF(player);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new PluginMessageTask(b).runTaskAsynchronously(BungeeSuiteBans.instance);
	}

	
	

}
