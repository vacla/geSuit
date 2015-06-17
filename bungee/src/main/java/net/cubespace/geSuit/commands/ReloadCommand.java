package net.cubespace.geSuit.commands;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.managers.AnnouncementManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /gsreload Permission needed: gesuit.reload or gesuit.admin Arguments: none What does it do: Reloads every config
 */
public class ReloadCommand extends Command
{

    public ReloadCommand()
    {
        super("gsreload");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender.hasPermission("gesuit.reload") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, Global.getMessages().get("player.no-permisison"));

            return;
        }

        try {
            ConfigManager.announcements.reload();
            ConfigManager.bans.reload();
            ConfigManager.main.reload();
            ConfigManager.spawn.reload();
            geSuit.getPlugin().loadLanguage();
            ConfigManager.teleport.reload();

            AnnouncementManager.reloadAnnouncements();
            PlayerManager.sendMessageToTarget(sender, "All Configs reloaded");
        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
            PlayerManager.sendMessageToTarget(sender, "Could not reload. Check the logs");
        }
    }
}
