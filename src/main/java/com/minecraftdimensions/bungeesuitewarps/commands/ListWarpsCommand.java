package com.minecraftdimensions.bungeesuitewarps.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.minecraftdimensions.bungeesuitewarps.BungeeSuiteWarps;
import com.minecraftdimensions.bungeesuitewarps.PermissionUtil;

public class ListWarpsCommand implements CommandExecutor {

	BungeeSuiteWarps plugin;
	private static final String[] PERMISSION_NODES = { "bungeesuite.warp.list", "bungeesuite.warp.*",
		"bungeesuite.mod", "bungeesuite.admin", "bungeesuite.*" };
	
	private static final String[] PERMISSION_NODES_OVERRIDE = { "bungeesuite.warp.list.admin",
		"bungeesuite.admin", "bungeesuite.*" };

	public ListWarpsCommand(BungeeSuiteWarps bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!PermissionUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		plugin.utils.getWarpList(sender.getName(), PermissionUtil.hasPermission(sender, PERMISSION_NODES_OVERRIDE));
		return true;
	}

}
