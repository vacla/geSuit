package com.minecraftdimensions.bungeesuitebans;

import com.minecraftdimensions.bungeesuitebans.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeSuiteBans extends JavaPlugin {

    public static String OUTGOING_PLUGIN_CHANNEL = "BSBans";
    public static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteBans";
    public static BungeeSuiteBans instance;

    @Override
    public void onEnable() {
        instance = this;
        registerChannels();
        registerCommands();
    }

    private void registerCommands() {
        getCommand( "ban" ).setExecutor( new BanCommand() );
        getCommand( "checkban" ).setExecutor( new CheckBanCommand() );
        getCommand( "ipban" ).setExecutor( new IPBanCommand() );
        getCommand( "kick" ).setExecutor( new KickCommand() );
        getCommand( "kickall" ).setExecutor( new KickAllCommand() );
        getCommand( "reloadbans" ).setExecutor( new ReloadBansCommand() );
        getCommand( "tempban" ).setExecutor( new TempBanCommand() );
        getCommand( "unban" ).setExecutor( new UnbanCommand() );
        getCommand( "unipban" ).setExecutor( new UnBanIPCommand() );
    }

    private void registerChannels() {
        this.getServer().getMessenger().registerOutgoingPluginChannel( this, OUTGOING_PLUGIN_CHANNEL );
        Bukkit.getMessenger().registerIncomingPluginChannel( this, INCOMING_PLUGIN_CHANNEL, new BansMessageListener() );
    }


}
