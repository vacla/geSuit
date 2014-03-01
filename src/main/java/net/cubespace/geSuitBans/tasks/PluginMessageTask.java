package net.cubespace.geSuitBans.tasks;

import net.cubespace.geSuitBans.geSuitBans;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;


public class PluginMessageTask extends BukkitRunnable {
    
    private final ByteArrayOutputStream bytes;
    
    public PluginMessageTask(ByteArrayOutputStream bytes) {
        this.bytes = bytes;
    }

    public void run() {
        Bukkit.getOnlinePlayers()[0].sendPluginMessage(geSuitBans.instance, "geSuitBans", bytes.toByteArray());
    }

}