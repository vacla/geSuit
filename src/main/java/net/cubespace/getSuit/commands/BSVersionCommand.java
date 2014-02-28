package net.cubespace.getSuit.commands;

import net.cubespace.getSuit.BungeeSuite;
import net.cubespace.getSuit.managers.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * User: Bloodsplat
 * Date: 13/10/13
 * <p/>
 * Current Maintainer: geNAZt
 * <p/>
 * Command: /bsversion
 * Permission needed: bungeesuite.version or bungeesuite.admin
 * Arguments: none or servername as first argument
 * What does it do: Gives you Version Information about BungeeSuitePlus on the given Server
 */
public class BSVersionCommand extends Command {
    public BSVersionCommand() {
        super("bsversion");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // If player does not have the Permission to lookup the Version
        if (!(sender.hasPermission("bungeesuite.version") || sender.hasPermission("bungeesuite.admin"))) {
            sender.sendMessage(ConfigManager.messages.NO_PERMISSION);

            return;
        }

        //Build up the GetVersion Message
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("GetVersion");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Send out the geSuit version
        sender.sendMessage(ChatColor.RED + "geSuit (BungeeSuite) version - " + ChatColor.GOLD + BungeeSuite.instance.getDescription().getVersion());

        if (args.length > 0) {
            //Check the Sever which is given in the Argument
            ServerInfo s = ProxyServer.getInstance().getServerInfo(args[0]);
            if (s == null) {
                sender.sendMessage(ChatColor.RED + "Server does not exist");
                return;
            }

            if (s.getPlayers().size() == 0) {
                sender.sendMessage(ChatColor.RED + "That server is either offline or there are no players on it");
                return;
            }

            BansManager.sendPluginMessageTaskBans(s, b);
            HomesManager.sendPluginMessageTaskHomes(s, b);
            PortalManager.sendPluginMessageTaskPortals(s, b);
            SpawnManager.sendPluginMessageTaskSpawns(s, b);
            TeleportManager.sendPluginMessageTaskTP(s, b);
            WarpsManager.sendPluginMessageTaskTP(s, b);
        } else {
            if (sender instanceof ProxiedPlayer) {
                //Check the Versions for the Server the Player is on
                ProxiedPlayer p = (ProxiedPlayer) sender;
                ServerInfo s = p.getServer().getInfo();

                BansManager.sendPluginMessageTaskBans(s, b);
                HomesManager.sendPluginMessageTaskHomes(s, b);
                PortalManager.sendPluginMessageTaskPortals(s, b);
                SpawnManager.sendPluginMessageTaskSpawns(s, b);
                TeleportManager.sendPluginMessageTaskTP(s, b);
                WarpsManager.sendPluginMessageTaskTP(s, b);
            } else {
                sender.sendMessage("Could not get Versions. You are not ingame or you have not given a Argument");
            }
        }
    }
}
