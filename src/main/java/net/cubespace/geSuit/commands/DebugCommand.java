package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * Command: /gsreload Permission needed: gesuit.reload or gesuit.admin Arguments: none What does it do: Reloads every config
 */
public class DebugCommand extends Command
{

    public DebugCommand()
    {
        super("gsdebug");
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (!(sender.hasPermission("gesuit.debug") || sender.hasPermission("gesuit.admin"))) {
            PlayerManager.sendMessageToTarget(sender, ConfigManager.messages.NO_PERMISSION);

            return;
        }

        // Toggle debug
        geSuit.instance.setDebugEnabled(!geSuit.instance.isDebugEnabled());
        
        PlayerManager.sendMessageToTarget(sender, "geSuit debug is now: " + (geSuit.instance.isDebugEnabled() ? "ENABLED" : "DISABLED"));
    }
}
