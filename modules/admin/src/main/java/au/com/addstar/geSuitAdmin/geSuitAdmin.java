package au.com.addstar.geSuitAdmin;

import au.com.addstar.geSuitAdmin.commands.DebugCommand;
import au.com.addstar.geSuitAdmin.listeners.AdminListener;
import net.cubespace.geSuit.BukkitModule;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class geSuitAdmin extends BukkitModule {

    public geSuitAdmin() {
        super("admin", false);
        setDebug(false);
    }

    @Override
    protected void registerListeners() {
        registerPluginMessageListener(this,new AdminListener(this));
    }
    
    protected void registerCommands() {
        getCommand("gesuitdebug").setExecutor(new DebugCommand(this));
    }
}
