package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;
import net.cubespace.geSuit.utils.Utilities;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class TempBanCommand extends CommandManager<BansManager> {

    public TempBanCommand(BansManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {

        if(args.length > 1){
            String player = args[0];
            String timing = args[1];
            String reason = StringUtils.join(args, " ", 2, args.length);
            int seconds = Utilities.parseStringToSecs(timing);
            if (seconds == 0) {
                sender.sendMessage("&c Couldn't convert "+args[1]+" to seconds");
                return false;
            }
            manager.tempBanPlayer(sender.getName(), player, seconds, reason);
            return true;
        }

        return false;
    }

}
