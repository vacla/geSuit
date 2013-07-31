package com.minecraftdimensions.bungeesuitewarps.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.minecraftdimensions.bungeesuitewarps.BungeeSuiteWarps;
import com.minecraftdimensions.bungeesuitewarps.PermissionUtil;

public class WarpCommand implements CommandExecutor {
	BungeeSuiteWarps plugin;
	private static final String[] PERMISSION_NODES = { "bungeesuite.warp.warp",
			"bungeesuite.warp.*", "bungeesuite.mod", "bungeesuite.admin",
			"bungeesuite.*" };
	private static final String[] WARP_PERMISSION_NODES = { "bungeesuite.warp.warp.*",
		"bungeesuite.warp.*", "bungeesuite.mod", "bungeesuite.admin",
		"bungeesuite.*" };
	private static final String[] PRIVATE_PERMISSION_NODES = { "bungeesuite.warp.private",
		"bungeesuite.warp.*", "bungeesuite.mod", "bungeesuite.admin",
		"bungeesuite.*" };
	private static final String[] PERMISSION_NODES_OVERRIDE = { "bungeesuite.warp.list.admin",
		"bungeesuite.admin", "bungeesuite.*" };
	private static final String[] WARP_OTHER_NODES = { "bungeesuite.warp.other",
		"bungeesuite.warp.*", "bungeesuite.mod", "bungeesuite.admin",
		"bungeesuite.*" };

	public WarpCommand(BungeeSuiteWarps bungeeSuiteTeleports) {
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
			if (!(PermissionUtil.hasPermission(sender, WARP_PERMISSION_NODES) || sender.hasPermission("bungeesuite.warp.warp."+args[0]))) {
				plugin.utils.getMessage(sender.getName(), "WARP_NO_PERMISSION");
				return true;
			}
			plugin.utils.warpRequest(sender.getName(),sender.getName(), args[0], PermissionUtil.hasPermission(sender, PRIVATE_PERMISSION_NODES));
			return true;
		}else if(args.length>=2 ){
			if(PermissionUtil.hasPermission(sender, WARP_OTHER_NODES)){
			plugin.utils.warpRequest(sender.getName(),args[1], args[0], PermissionUtil.hasPermission(sender, PRIVATE_PERMISSION_NODES));
			return true;
			}else{
				plugin.utils.getMessage(sender.getName(), "WARP_NO_PERMISSION");
				return true;
			}
		}
		plugin.utils.getWarpList(sender.getName(), PermissionUtil.hasPermission(sender, PERMISSION_NODES_OVERRIDE));
		return true;
	}

}
