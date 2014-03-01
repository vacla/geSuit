package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.AnnouncementManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /gsreload
 * Permission needed: gesuit.reload or gesuit.admin
 * Arguments: none
 * What does it do: Reloads every config
 */
public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("gsreload");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender.hasPermission("gesuit.reload") || sender.hasPermission("gesuit.admin"))) {
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
