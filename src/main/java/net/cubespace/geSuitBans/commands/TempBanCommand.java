package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuitBans.utils.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import net.cubespace.geSuitBans.managers.BansManager;

import java.lang.reflect.Array;
import java.util.Arrays;


public class TempBanCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if(args.length > 1){
			BansManager.tempBanPlayer(sender.getName(), args[0], args[1], (args.length > 2) ? StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ") : "");
			return true;
		}

		return false;
	}

}
