package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /motd
 * Permission needed: gesuit.motd or gesuit.admin
 * Arguments: none
 * What does it do: Prints out the MOTD
 */
public class MOTDCommand extends Command {
    public MOTDCommand() {
        super("motd");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("gesuit.motd") || sender.hasPermission("gesuit.admin"))) {
            sender.sendMessage(Utilities.colorize(ConfigManager.messages.NO_PERMISSION));

            return;
        }

        for (String split : ConfigManager.messages.MOTD.split("\n")) {
            sender.sendMessage(Utilities.colorize(split));
        }
    }
}


