package au.com.addstar.geSuitAdmin;

import au.com.addstar.geSuitAdmin.commands.DebugCommand;
import au.com.addstar.geSuitAdmin.listeners.AdminListener;
import net.cubespace.geSuit.BukkitModule;
import org.bukkit.Bukkit;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class geSuitAdmin extends BukkitModule {
    private boolean debug;
    
    protected geSuitAdmin() {
        super("admin");
        debug =  false;
    }
    
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    protected void registerChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this,
                getCHANNEL_NAME(), new AdminListener(this));
    }

    protected void registerCommands() {
        getCommand("gsAdmin_debug").setExecutor(new DebugCommand(this));
    }
}
