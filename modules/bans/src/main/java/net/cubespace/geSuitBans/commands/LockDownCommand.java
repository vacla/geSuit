package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuitBans.managers.BansManager;
import net.cubespace.geSuit.utils.Utilities;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Author: narimm on 26/08/2015.
 */
public class LockDownCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission("gesuit.ban.command.lockdown")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do that command.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /lockdown end|status|<time> <msg>.");
            return true;
        }

        if (Objects.equals(StringUtils.uncapitalize(args[0]), "end")) {
            BansManager.endLockDown(sender.getName());
            return true;
        }
        if (Objects.equals(StringUtils.uncapitalize(args[0]), "status")) {
            BansManager.lockDownStatus(sender.getName());
            return true;
        }
        long expiryTime = getExpiryTime(sender, args[0]);
        if (expiryTime > 0) {
            if (args.length == 1) {
                BansManager.lockDown(sender.getName(), expiryTime, "");
                return true;
            }
            if (args.length > 1) {
                BansManager.lockDown(sender.getName(), expiryTime, StringUtils.join(args, " ", 1, args.length));
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Usage: /lockdown end|status|<time> <msg>.");
        return true;
    }

    /**
     * Converts timeString in the format 5d3h2m3s to millisecs and returns it as a long.
     * using w(eeks), d(ays), h(ours), m(inutes) and s(econds) For example: 4d8m2s -> 4 days, 8 minutes and 2 seconds
     *
     * @param sender the command sender
     * @param time   a string in the format
     * @return long which is a time
     */
    private long getExpiryTime(CommandSender sender, String time) {
        try {
            long timemillisec = Utilities.parseStringtoMillisecs(time);
            if (timemillisec > 0) {
                return System.currentTimeMillis() + timemillisec;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "You specified an invalid time string: " + time);
        }
        return 0;
    }

}

