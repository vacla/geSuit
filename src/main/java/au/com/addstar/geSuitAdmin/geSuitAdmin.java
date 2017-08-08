package au.com.addstar.geSuitAdmin;

import au.com.addstar.geSuitAdmin.listeners.AdminListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class geSuitAdmin extends JavaPlugin {


    @Override
    public void onDisable() {
        deRegisterChannels();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        registerChannels();
    }

    private void registerChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this,
                "geSuitAdmin", new AdminListener(this));
    }

    private void deRegisterChannels(){
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
    }
}
