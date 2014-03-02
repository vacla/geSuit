package net.cubespace.geSuit;

import net.cubespace.geSuit.commands.MOTDCommand;
import net.cubespace.geSuit.commands.ReloadCommand;
import net.cubespace.geSuit.database.ConnectionHandler;
import net.cubespace.geSuit.database.convert.Converter;
import net.cubespace.geSuit.listeners.BansListener;
import net.cubespace.geSuit.listeners.BansMessageListener;
import net.cubespace.geSuit.listeners.HomesMessageListener;
import net.cubespace.geSuit.listeners.PlayerListener;
import net.cubespace.geSuit.listeners.PortalsMessageListener;
import net.cubespace.geSuit.listeners.SpawnListener;
import net.cubespace.geSuit.listeners.SpawnMessageListener;
import net.cubespace.geSuit.listeners.TeleportsMessageListener;
import net.cubespace.geSuit.listeners.WarpsMessageListener;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.DatabaseManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class geSuit extends Plugin {
    public static geSuit instance;
    public static ProxyServer proxy;

    public void onEnable() {
        instance = this;
        LoggingManager.log( ChatColor.GREEN + "Starting geSuit" );
        proxy = ProxyServer.getInstance();
        LoggingManager.log( ChatColor.GREEN + "Initialising Managers" );

        ConnectionHandler connectionHandler = DatabaseManager.connectionPool.getConnection();
        connectionHandler.release();

        if(ConfigManager.main.ConvertFromBungeeSuite) {
            Converter converter = new Converter();
            converter.convert();
        }

        registerListeners();
        registerCommands();
    }

    private void registerCommands() {
        proxy.getPluginManager().registerCommand( this, new MOTDCommand() );
        proxy.getPluginManager().registerCommand( this, new ReloadCommand() );
    }

    private void registerListeners() {
        getProxy().registerChannel("geSuitTeleport");       // Teleport out/in
        getProxy().registerChannel("geSuitSpawns");         // Spawns out/in
        getProxy().registerChannel("geSuitBans");           // Bans in
        getProxy().registerChannel("geSuitPortals");        // Portals out/in
        getProxy().registerChannel("geSuitWarps");          // Warps in
        getProxy().registerChannel("geSuitHomes");          // Homes in

        proxy.getPluginManager().registerListener( this, new PlayerListener() );
        proxy.getPluginManager().registerListener( this, new BansMessageListener() );
        proxy.getPluginManager().registerListener( this, new BansListener() );
        proxy.getPluginManager().registerListener( this, new TeleportsMessageListener() );
        proxy.getPluginManager().registerListener( this, new WarpsMessageListener() );
        proxy.getPluginManager().registerListener( this, new HomesMessageListener() );
        proxy.getPluginManager().registerListener( this, new PortalsMessageListener() );
        proxy.getPluginManager().registerListener( this, new SpawnListener() );
        proxy.getPluginManager().registerListener( this, new SpawnMessageListener() );
    }

    public void onDisable() {
        DatabaseManager.connectionPool.closeConnections();
    }
}
