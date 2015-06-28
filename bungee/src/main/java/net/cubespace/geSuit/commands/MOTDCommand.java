package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.core.Global;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /motd
 * Permission needed: gesuit.motd or gesuit.admin
 * Arguments: none
 * What does it do: Prints out the MOTD
 */
public class MOTDCommand extends Command {
    private ConfigManager configManager;
    public MOTDCommand(ConfigManager configManager) {
        super("motd");
        
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("gesuit.motd") || sender.hasPermission("gesuit.admin"))) {
            sender.sendMessage(Global.getMessages().get("player.no-permission"));

            return;
        }
        sender.sendMessage(configManager.getMOTD(false).replace("{player}", sender.getName()));
    }
}


