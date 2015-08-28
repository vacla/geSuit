package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuitBans.managers.BansManager;
import net.cubespace.geSuitBans.utils.TimeParser;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TempBanCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {

        if(args.length > 1){
            String player = args[0];
            String timing = args[1];
            String reason = StringUtils.join(args, " ", 2, args.length);
            int seconds = TimeParser.parseStringToSecs(timing);
            if (seconds == 0) {
                sender.sendMessage("&c Couldn't convert "+args[1]+" to seconds");
                return false;
            }
            BansManager.tempBanPlayer(sender.getName(), player, seconds, reason);
            return true;
        }

        return false;
    }

}
