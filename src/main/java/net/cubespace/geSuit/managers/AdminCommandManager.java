package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.objects.AdminCommand;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.pluginmessages.SendAdminCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class AdminCommandManager {

    public static void sendAdminCommand(CommandSender sender, String server, String command, String... args){
        AdminCommand adminCommand =  new AdminCommand(command, server,sender.getName(),args);
        if(ProxyServer.getInstance().getConfigurationAdapter().getServers().containsKey(server)){
            SendAdminCommand.execute(adminCommand);
        }else{
            PlayerManager.sendMessageToTarget(sender,"No server with name " +server);
        }
    }


}
