package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created for the AddstarMC Project. Created by Narimm on 19/09/2018.
 */
public class EnableBukkitDebug {
    public static void execute(ServerInfo server) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("EnableDebug");
        } catch (IOException e) {
            e.printStackTrace();
        }
        geSuit.proxy.getScheduler().runAsync(geSuit.getInstance(),
                new SendPluginMessage(geSuit.CHANNEL_NAMES.ADMIN_CHANNEL,
                        server, bytes));
    }
}
