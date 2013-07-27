package com.minecraftdimensions.bungeesuiteportals;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetPortalCommand implements CommandExecutor {

	BungeeSuitePortals plugin;
	
	private static final String[] PERMISSION_NODES = { "bungeesuite.portal.create", "bungeesuite.portal.*",
		"bungeesuite.admin", "bungeesuite.*" };

	public SetPortalCommand(BungeeSuitePortals bungeeSuitePortals){
		plugin = bungeeSuitePortals;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!CommandUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		Region region = plugin.rsm.getSelection(sender.getName());
		if(region==null){
			sender.sendMessage(ChatColor.RED+"Please select an area with the wooden axe");
			return true;
		}
		if(args.length==3){
			plugin.utils.createPortal(sender.getName(), args[0], args[1], args[2],((Player)sender).getWorld().getName(), "WATER", plugin.rsm.getSelection(sender.getName()));
			return true;
		}else if(args.length==4){
			plugin.utils.createPortal(sender.getName(), args[0], args[1], args[2],((Player)sender).getWorld().getName(), args[3].toUpperCase(), plugin.rsm.getSelection(sender.getName()));
			return true;
		}
		
			return false;
		
	}


}
