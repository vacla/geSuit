package com.minecraftdimensions.bungeesuitewarps.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.minecraftdimensions.bungeesuitewarps.BungeeSuiteWarps;
import com.minecraftdimensions.bungeesuitewarps.PermissionUtil;


public class SetWarpCommand implements CommandExecutor {

	BungeeSuiteWarps plugin;
	private static final String[] PERMISSION_NODES = { "bungeesuite.warp.create",
		"bungeesuite.admin", "bungeesuite.*" };
	
	public SetWarpCommand(BungeeSuiteWarps bungeeSuiteTeleports){
		plugin = bungeeSuiteTeleports;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if (!PermissionUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if(args.length==1){
			Location loc = ((Player)sender).getLocation();
			plugin.utils.createWarp(sender.getName(),args[0], loc.getWorld().getName(), loc.getX(),loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),false);
			return true;
		}else if(args.length==2 && (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("private"))){
			Location loc = ((Player)sender).getLocation();
			plugin.utils.createWarp(sender.getName(),args[0], loc.getWorld().getName(), loc.getX(),loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),true);
			return true;
		}
				return false;

	}

}

