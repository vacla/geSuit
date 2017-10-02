package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.configs.Messages;
import net.cubespace.geSuit.managers.PlayerManager;
import net.cubespace.geSuit.managers.SpawnManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 2/10/2017.
 */
public class BacktoNewSpawn extends Command {

    public BacktoNewSpawn() {
        super("!newSpawn", "gesuit.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }
        if (args.length == 0) {
            PlayerManager.sendMessageToTarget(sender, Messages.BUNGEE_COMMAND_NEWSPAWN_USAGE);
            return;
        }
        SpawnManager.sendPlayerToNewPlayerSpawn(sender, args[0]);
    }
}
