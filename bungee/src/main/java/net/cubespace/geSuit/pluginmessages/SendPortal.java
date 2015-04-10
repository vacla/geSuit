package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.geSuit.objects.Location;
import net.cubespace.geSuit.objects.Portal;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class SendPortal {
    public static String OUTGOING_CHANNEL = "geSuitPortals";

    public static void execute(Portal p) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("SendPortal");
            out.writeUTF(p.getName());
            out.writeUTF(p.getType());
            out.writeUTF(p.getDest());
            out.writeUTF(p.getFillType());
            Location max = p.getMax();
            out.writeUTF(max.getWorld());
            out.writeDouble(max.getX());
            out.writeDouble(max.getY());
            out.writeDouble(max.getZ());
            Location min = p.getMin();
            out.writeUTF(min.getWorld());
            out.writeDouble(min.getX());
            out.writeDouble(min.getY());
            out.writeDouble(min.getZ());
        } catch (IOException e) {
            e.printStackTrace();
        }

        geSuitPlugin.proxy.getScheduler().runAsync(geSuit.getPlugin(), new SendPluginMessage(OUTGOING_CHANNEL, p.getServer(), bytes));
    }
}
