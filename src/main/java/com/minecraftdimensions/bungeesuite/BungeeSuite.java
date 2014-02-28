package com.minecraftdimensions.bungeesuite;

import com.minecraftdimensions.bungeesuite.commands.BSVersionCommand;
import com.minecraftdimensions.bungeesuite.commands.MOTDCommand;
import com.minecraftdimensions.bungeesuite.commands.ReloadCommand;
import com.minecraftdimensions.bungeesuite.listeners.BansListener;
import com.minecraftdimensions.bungeesuite.listeners.BansMessageListener;
import com.minecraftdimensions.bungeesuite.listeners.HomesMessageListener;
import com.minecraftdimensions.bungeesuite.listeners.PlayerListener;
import com.minecraftdimensions.bungeesuite.listeners.PortalsMessageListener;
import com.minecraftdimensions.bungeesuite.listeners.SpawnListener;
import com.minecraftdimensions.bungeesuite.listeners.SpawnMessageListener;
import com.minecraftdimensions.bungeesuite.listeners.TeleportsMessageListener;
import com.minecraftdimensions.bungeesuite.listeners.WarpsMessageListener;
import com.minecraftdimensions.bungeesuite.managers.AnnouncementManager;
import com.minecraftdimensions.bungeesuite.managers.DatabaseTableManager;
import com.minecraftdimensions.bungeesuite.managers.LoggingManager;
import com.minecraftdimensions.bungeesuite.managers.PortalManager;
import com.minecraftdimensions.bungeesuite.managers.SQLManager;
import com.minecraftdimensions.bungeesuite.managers.SpawnManager;
import com.minecraftdimensions.bungeesuite.managers.TeleportManager;
import com.minecraftdimensions.bungeesuite.managers.WarpsManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;

public class BungeeSuite extends Plugin {
    public static BungeeSuite instance;
    public static ProxyServer proxy;

    public void onEnable() {
        instance = this;
        LoggingManager.log( ChatColor.GREEN + "Starting BungeeSuite" );
        proxy = ProxyServer.getInstance();
        LoggingManager.log( ChatColor.GREEN + "Initialising Managers" );
        initialiseManagers();
        registerListeners();
        registerCommands();
    }

    private void registerCommands() {
        proxy.getPluginManager().registerCommand( this, new BSVersionCommand() );
        proxy.getPluginManager().registerCommand( this, new MOTDCommand() );
        proxy.getPluginManager().registerCommand( this, new ReloadCommand() );
    }

    private void initialiseManagers() {
        if ( SQLManager.initialiseConnections() ) {
            DatabaseTableManager.createDefaultTables();
            AnnouncementManager.loadAnnouncements();
            TeleportManager.initialise();
            try {
                WarpsManager.loadWarpLocations();
                PortalManager.loadPortals();
                SpawnManager.loadSpawns();
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        } else {
            LoggingManager.log( ChatColor.DARK_RED + "Your BungeeSuite is unable to connect to your SQL database specified in the config" );
        }
    }

    void registerListeners() {
        this.getProxy().registerChannel( "BSChat" );//in
        this.getProxy().registerChannel( "BungeeSuiteChat" );//out
        this.getProxy().registerChannel( "BSBans" );//in
        this.getProxy().registerChannel( "BungeeSuiteBans" ); //out
        this.getProxy().registerChannel( "BSTeleports" );//in
        this.getProxy().registerChannel( "BungeeSuiteTP" );//out
        this.getProxy().registerChannel( "BSWarps" );//in
        this.getProxy().registerChannel( "BungeeSuiteWarps" );//out
        this.getProxy().registerChannel( "BSHomes" );//in
        this.getProxy().registerChannel( "BungeeSuiteHomes" );//out
        this.getProxy().registerChannel( "BSPortals" );//in
        this.getProxy().registerChannel( "BungeeSuitePorts" );//out
        this.getProxy().registerChannel( "BSSpawns" );//in
        this.getProxy().registerChannel( "BungeeSuiteSpawn" );//out
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
        SQLManager.closeConnections();
    }
}
