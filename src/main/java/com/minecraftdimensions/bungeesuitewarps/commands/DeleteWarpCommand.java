package com.minecraftdimensions.bungeesuitewarps.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.minecraftdimensions.bungeesuitewarps.BungeeSuiteWarps;
import com.minecraftdimensions.bungeesuitewarps.PermissionUtil;

public class DeleteWarpCommand implements CommandExecutor {

	BungeeSuiteWarps plugin;
	private static final String[] PERMISSION_NODES = {
			"bungeesuite.warp.delete", "bungeesuite.admin", "bungeesuite.*" };

	public DeleteWarpCommand(BungeeSuiteWarps bungeeSuiteTeleports) {
		plugin = bungeeSuiteTeleports;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (!PermissionUtil.hasPermission(sender, PERMISSION_NODES)) {
			plugin.utils.getMessage(sender.getName(), "NO_PERMISSION");
			return true;
		}
		if (args.length >= 1) {
			plugin.utils.deleteWarp(sender.getName(), args[0]);
			return true;
		}
		return false;

	}

}
