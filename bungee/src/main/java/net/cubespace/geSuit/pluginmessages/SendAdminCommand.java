package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.AdminCommand;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.ProxyServer;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class SendAdminCommand {
    public static String OUTGOING_CHANNEL = "geSuitAdmin";
    public static void execute(AdminCommand adminCommand) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        if (adminCommand.command.equals("restart")) {
            long time = 0L;
            if(adminCommand.getArgs().size() >= 1){
                time = TimeParser.parseStringtoMillisecs(adminCommand.getArgs().get(0));
                if (time == 0) time = 10000L;
            }
            try {
                // servername sender milliSecs
                out.writeUTF("ServerRestart");
                out.writeUTF(adminCommand.getServer());
                out.writeUTF(adminCommand.getCommandSender());
                out.writeLong(time);
            } catch (IOException e) {
                e.printStackTrace();
            }
            geSuit.proxy.getScheduler().runAsync(geSuit.getInstance(),
                new SendPluginMessage(geSuit.CHANNEL_NAMES.ADMIN_CHANNEL,
                    ProxyServer.getInstance().getServerInfo(adminCommand.getServer()), bytes));
        }else{
            //not supported
        }
    }
}
