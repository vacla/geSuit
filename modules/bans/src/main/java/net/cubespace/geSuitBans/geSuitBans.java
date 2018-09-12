package net.cubespace.geSuitBans;

import net.cubespace.geSuitBans.commands.*;
import net.cubespace.geSuit.BukkitModule;

public class geSuitBans extends BukkitModule {
    
    geSuitBans() {
        super("bans");
    }
    
    protected void registerCommands() {
        getCommand( "ban" ).setExecutor( new BanCommand() );
        getCommand( "warn" ).setExecutor( new WarnCommand() );
        getCommand( "checkban" ).setExecutor( new CheckBanCommand() );
        getCommand( "banhistory" ).setExecutor( new BanHistoryCommand() );
        getCommand( "warnhistory" ).setExecutor( new WarnHistoryCommand() );
        getCommand( "where" ).setExecutor( new WhereCommand() );
        getCommand( "ipban" ).setExecutor( new IPBanCommand() );
        getCommand( "kick" ).setExecutor( new KickCommand() );
        getCommand( "kickall" ).setExecutor( new KickAllCommand() );
        getCommand( "reloadbans" ).setExecutor( new ReloadBansCommand() );
        getCommand( "tempban" ).setExecutor( new TempBanCommand() );
        getCommand( "unban" ).setExecutor( new UnbanCommand() );
        getCommand( "unipban" ).setExecutor( new UnBanIPCommand() );
        getCommand( "ontime" ).setExecutor( new OnTimeCommand() );
        getCommand( "lastlogins" ).setExecutor( new LastLoginsCommand());
        getCommand("namehistory").setExecutor( new NameHistoryCommand() );
        getCommand("lockdown").setExecutor(new LockDownCommand());
        getCommand("newSpawn").setExecutor(new NewSpawnCommand());

    }
    
    protected void registerChannels() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, getCHANNEL_NAME());
    }
}
