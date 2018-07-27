package net.cubespace.geSuit.pluginmessages;

import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Portal;
import net.cubespace.geSuit.tasks.SendPluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class DeletePortal {
    
    public static void execute(Portal p) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("DeletePortal");
            out.writeUTF(p.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        geSuit.proxy.getScheduler().runAsync(geSuit.instance, new SendPluginMessage(geSuit
                .CHANNEL_NAMES.PORTAL_CHANNEL.toString(), p
                .getServer(), bytes));
    }
}
