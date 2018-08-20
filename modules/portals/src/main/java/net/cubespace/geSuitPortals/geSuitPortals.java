package net.cubespace.geSuitPortals;


import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.cubespace.geSuitPortals.commands.DeletePortalCommand;
import net.cubespace.geSuitPortals.commands.ListPortalsCommand;
import net.cubespace.geSuitPortals.commands.SetPortalCommand;
import net.cubespace.geSuitPortals.listeners.AntiBurnListener;
import net.cubespace.geSuitPortals.listeners.PhysicsListener;
import net.cubespace.geSuitPortals.listeners.PlayerLoginListener;
import net.cubespace.geSuitPortals.listeners.PlayerMoveListener;
import net.cubespace.geSuitPortals.listeners.PortalsMessageListener;
import net.cubespace.geSuitPortals.managers.PortalsManager;
import net.cubespace.geSuitPortals.objects.Portal;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class geSuitPortals extends JavaPlugin {
    public static geSuitPortals INSTANCE = null;
    public static WorldEditPlugin WORLDEDIT = null;
    public static String CHANNEL_NAME = "bungeecord:geSuitPortals";

    @Override
    public void onEnable() {
        INSTANCE = this;
        loadWorldEdit();
        registerListeners();
        registerChannels();
        registerCommands();
    }

    @Override
    public void onDisable() {
        for (ArrayList<Portal> list : PortalsManager.PORTALS.values()) {
            for (Portal p : list) {
                p.clearPortal();
            }
        }
    }


    private void loadWorldEdit() {
        WORLDEDIT = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
    }

    private void registerCommands() {
        getCommand("setportal").setExecutor(new SetPortalCommand());
        getCommand("delportal").setExecutor(new DeletePortalCommand());
        getCommand("portals").setExecutor(new ListPortalsCommand());
    }

    private void registerChannels() {
        Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL_NAME, new PortalsMessageListener
                ());
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PhysicsListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);
        getServer().getPluginManager().registerEvents(new AntiBurnListener(), this);
    }
}
