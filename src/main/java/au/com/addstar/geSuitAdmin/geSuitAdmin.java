package au.com.addstar.geSuitAdmin;

import au.com.addstar.geSuitAdmin.commands.DebugCommand;
import au.com.addstar.geSuitAdmin.listeners.AdminListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class geSuitAdmin extends JavaPlugin {
    private static geSuitAdmin INSTANCE = null;
    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void onDisable() {
        deRegisterChannels();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        INSTANCE = this;
        registerChannels();
        registerCommands();
        debug =  false;
    }

    private void registerChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this,
                "geSuitAdmin", new AdminListener(this));

    }

    private void deRegisterChannels(){
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
    }
    private void registerCommands() {
        getCommand("gsAdmin_debug").setExecutor(new DebugCommand(INSTANCE));
    }
}
