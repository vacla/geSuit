package net.cubespace.geSuitBans.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;


public class KickAllCommand extends CommandManager<BansManager> {


    public KickAllCommand(BansManager manager) {
        super(manager);
    }

    @Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

        manager.kickAll(sender.getName(), StringUtils.join(args, " "));
		return true;
	}

}
