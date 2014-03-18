package net.cubespace.geSuitBans.tasks;

import java.io.ByteArrayOutputStream;
import net.cubespace.geSuitBans.geSuitBans;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginMessageTask extends BukkitRunnable
{

    private final ByteArrayOutputStream bytes;

    public PluginMessageTask(ByteArrayOutputStream bytes)
    {
        this.bytes = bytes;
    }

    public void run()
    {
        if (Bukkit.getOnlinePlayers().length == 0) {
            geSuitBans.instance.getLogger().info("Tried to send a pluginMessage with an empty server. Cancelling.");
        }
        else {
            Bukkit.getOnlinePlayers()[0].sendPluginMessage(geSuitBans.instance, "geSuitBans", bytes.toByteArray());
        }
    }

}
