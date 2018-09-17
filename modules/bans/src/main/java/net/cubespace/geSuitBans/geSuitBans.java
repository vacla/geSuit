package net.cubespace.geSuitBans;

import net.cubespace.geSuitBans.commands.*;
import net.cubespace.geSuit.BukkitModule;
import net.cubespace.geSuitBans.managers.BansManager;

public class geSuitBans extends BukkitModule {

    public geSuitBans() {
        super("bans",true);
    }
    
    protected void registerCommands() {
        BansManager manager = new BansManager(this);
        getCommand("ban").setExecutor(new BanCommand(manager, this));
        getCommand("warn").setExecutor(new WarnCommand(manager));
        getCommand("checkban").setExecutor(new CheckBanCommand(manager));
        getCommand("banhistory").setExecutor(new BanHistoryCommand(manager));
        getCommand("warnhistory").setExecutor(new WarnHistoryCommand(manager));
        getCommand("where").setExecutor(new WhereCommand(manager));
        getCommand("ipban").setExecutor(new IPBanCommand(manager));
        getCommand("kick").setExecutor(new KickCommand(manager));
        getCommand("kickall").setExecutor(new KickAllCommand(manager));
        getCommand("reloadbans").setExecutor(new ReloadBansCommand(manager));
        getCommand("tempban").setExecutor(new TempBanCommand(manager));
        getCommand("unban").setExecutor(new UnbanCommand(manager));
        getCommand("unipban").setExecutor(new UnBanIPCommand(manager));
        getCommand("ontime").setExecutor(new OnTimeCommand(manager));
        getCommand("lastlogins").setExecutor(new LastLoginsCommand(manager));
        getCommand("namehistory").setExecutor(new NameHistoryCommand(manager));
        getCommand("lockdown").setExecutor(new LockDownCommand(manager));
        getCommand("newSpawn").setExecutor(new NewSpawnCommand(manager));
    
    }
}
