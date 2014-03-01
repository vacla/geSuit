package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class TeleportToPlayer {
    public static String OUTGOING_CHANNEL = "geSuitTeleport";

    public static void execute(GSPlayer player, GSPlayer target) {
        if (!player.getServer().getInfo().equals(target.getServer().getInfo())) {
            player.getProxiedPlayer().connect(target.getServer().getInfo());
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("TeleportToPlayer");
            out.writeUTF(player.getName());
            out.writeUTF(target.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, target.getServer().getInfo(), bytes));
    }
}
