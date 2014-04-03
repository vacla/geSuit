package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.GSPlayer;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class TeleportToLocation
{

    public static String OUTGOING_CHANNEL = "geSuitTeleport";

    public static void execute(GSPlayer player, Location location)
    {
        if (location.getServer() == null) {
            geSuit.instance.getLogger().severe("Location has no Server, this should never happen. Please check");
            Exception exception = new Exception("");
            exception.printStackTrace();
        }

        if (player.getServer() == null || !player.getServer().equals(location.getServer().getName())) {
            player.getProxiedPlayer().connect(location.getServer());
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("TeleportToLocation");
            out.writeUTF(player.getName());
            out.writeUTF(location.getWorld());
            out.writeDouble(location.getX());
            out.writeDouble(location.getY());
            out.writeDouble(location.getZ());
            out.writeFloat(location.getYaw());
            out.writeFloat(location.getPitch());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(OUTGOING_CHANNEL, location.getServer(), bytes));
    }
}
