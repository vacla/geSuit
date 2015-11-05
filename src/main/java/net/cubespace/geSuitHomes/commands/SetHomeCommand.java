package net.cubespace.geSuitHomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuitHomes.managers.HomesManager;

import javax.annotation.RegEx;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetHomeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(args.length==0){
			HomesManager.setHome(sender, "home");
		}else{
			Pattern invalidChar = Pattern.compile("[^a-zA-Z0-9_-]");
			Matcher reMatch = invalidChar.matcher(args[0]);

			if (reMatch.find()) {
				sender.sendMessage(ChatColor.RED + "Invalid character in new home name: " + ChatColor.YELLOW + reMatch.group(0));
				return true;
			}

			HomesManager.setHome(sender, args[0]);
		}
		return true;
	}

}
