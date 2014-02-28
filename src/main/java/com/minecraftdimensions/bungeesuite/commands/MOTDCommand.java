package com.minecraftdimensions.bungeesuite.commands;

import com.minecraftdimensions.bungeesuite.configs.Messages;
import com.minecraftdimensions.bungeesuite.managers.ConfigManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * User: Bloodsplat
 * Date: 13/10/13
 * <p/>
 * Current Maintainer: geNAZt
 * <p/>
 * Command: /motd
 * Permission needed: bungeesuite.motd or bungeesuite.admin
 * Arguments: none
 * What does it do: Prints out the MOTD
 */
public class MOTDCommand extends Command {
    public MOTDCommand() {
        super("motd");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("bungeesuite.motd") || sender.hasPermission("bungeesuite.admin"))) {
            sender.sendMessage(ConfigManager.messages.NO_PERMISSION);

            return;
        }

        for (String split : ConfigManager.messages.MOTD.split("\n")) {
            sender.sendMessage(split);
        }
    }
}


