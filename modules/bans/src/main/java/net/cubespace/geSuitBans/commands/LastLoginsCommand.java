package net.cubespace.geSuitBans.commands;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitBans.managers.BansManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Created by benjamincharlton on 18/08/2015.
 */
public class LastLoginsCommand extends CommandManager<BansManager> {
    public LastLoginsCommand(BansManager manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /lastlogins <playername> <num>.");
            return true;
        }
        int num = 5;
        if (args.length == 2) {
            try {
                num = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "You specified an invalid number.");
                return true;
            }
        }
        manager.displayLastLogins(sender.getName(), args[0], num);
        return true;
        }

    }

