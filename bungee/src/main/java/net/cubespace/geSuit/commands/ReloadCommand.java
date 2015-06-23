package net.cubespace.geSuit.commands;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /gsreload Permission needed: gesuit.reload or gesuit.admin Arguments: none What does it do: Reloads every config
 */
public class ReloadCommand extends Command
{
    private geSuitPlugin plugin;
    private ConfigManager configManager;
    public ReloadCommand(geSuitPlugin plugin, ConfigManager manager)
    {
        super("gsreload");
        this.plugin = plugin;
        this.configManager = manager;
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender.hasPermission("gesuit.reload") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, Global.getMessages().get("player.no-permission"));

            return;
        }

        try {
            configManager.reloadAll();
            plugin.loadLanguage();

            PlayerManager.sendMessageToTarget(sender, "All Configs reloaded");
        }
        catch (InvalidConfigurationException e) {
            e.printStackTrace();
            PlayerManager.sendMessageToTarget(sender, "Could not reload. Check the logs");
        }
    }
}
