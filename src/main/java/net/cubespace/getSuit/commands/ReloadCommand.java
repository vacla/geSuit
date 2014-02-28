package net.cubespace.getSuit.commands;

import net.cubespace.getSuit.managers.AnnouncementManager;
import net.cubespace.getSuit.managers.ConfigManager;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Current Maintainer: geNAZt
 * <p/>
 * Command: /bsreload
 * Permission needed: bungeesuite.reload or bungeesuite.admin
 * Arguments: none
 * What does it do: Reloads every config
 */
public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("bsreload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("bungeesuite.reload") || sender.hasPermission("bungeesuite.admin"))) {
            sender.sendMessage(ConfigManager.messages.NO_PERMISSION);

            return;
        }

        try {
            ConfigManager.announcements.reload();
            ConfigManager.bans.reload();
            ConfigManager.main.reload();
            ConfigManager.spawn.reload();
            ConfigManager.messages.reload();

            AnnouncementManager.reloadAnnouncements();
            sender.sendMessage("All Configs reloaded");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            sender.sendMessage("Could not reload. Check the logs");
        }
    }
}
