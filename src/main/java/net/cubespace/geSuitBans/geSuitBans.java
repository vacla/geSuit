package net.cubespace.geSuitBans;

import net.cubespace.geSuitBans.commands.BanCommand;
import net.cubespace.geSuitBans.commands.BanHistoryCommand;
import net.cubespace.geSuitBans.commands.CheckBanCommand;
import net.cubespace.geSuitBans.commands.IPBanCommand;
import net.cubespace.geSuitBans.commands.KickAllCommand;
import net.cubespace.geSuitBans.commands.KickCommand;
import net.cubespace.geSuitBans.commands.ReloadBansCommand;
import net.cubespace.geSuitBans.commands.TempBanCommand;
import net.cubespace.geSuitBans.commands.UnBanIPCommand;
import net.cubespace.geSuitBans.commands.UnbanCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class geSuitBans extends JavaPlugin {
    public static geSuitBans instance;

    @Override
    public void onEnable() {
        instance = this;
        registerChannels();
        registerCommands();
    }

    private void registerCommands() {
        getCommand( "ban" ).setExecutor( new BanCommand() );
        getCommand( "warn" ).setExecutor( new BanCommand() );
        getCommand( "checkban" ).setExecutor( new CheckBanCommand() );
        getCommand( "banhistory" ).setExecutor( new BanHistoryCommand() );
        getCommand( "warnhistory" ).setExecutor( new BanHistoryCommand() );
        getCommand( "ipban" ).setExecutor( new IPBanCommand() );
        getCommand( "kick" ).setExecutor( new KickCommand() );
        getCommand( "kickall" ).setExecutor( new KickAllCommand() );
        getCommand( "reloadbans" ).setExecutor( new ReloadBansCommand() );
        getCommand( "tempban" ).setExecutor( new TempBanCommand() );
        getCommand( "unban" ).setExecutor( new UnbanCommand() );
        getCommand( "unipban" ).setExecutor( new UnBanIPCommand() );
    }

    private void registerChannels() {
        this.getServer().getMessenger().registerOutgoingPluginChannel( this, "geSuitBans" );
    }
}
