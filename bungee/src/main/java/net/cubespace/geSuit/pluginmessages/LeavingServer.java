package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LeavingServer
{
    public static String OUTGOING_CHANNEL = "geSuitTeleport";

    public static void execute(GSPlayer player)
    {
        if (player.getServer() == null) {
            geSuit.getLogger().severe("Player has no Server, this should never happen. Please check");
            new Throwable().printStackTrace();
            return;
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("LeavingServer");
            out.writeUTF(player.getName());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        geSuitPlugin.proxy.getScheduler().runAsync(geSuit.getPlugin(), new SendPluginMessage(OUTGOING_CHANNEL, player.getProxiedPlayer().getServer().getInfo(), bytes));
    }
}
