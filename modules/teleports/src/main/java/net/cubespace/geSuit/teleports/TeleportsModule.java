package net.cubespace.geSuit.teleports;

import org.bukkit.Bukkit;

import com.google.common.base.Preconditions;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.remote.teleports.TeleportActions;
import net.cubespace.geSuit.teleports.commands.TeleportCommands;

public class TeleportsModule extends BaseModule {
    private TeleportActions teleports;
    private TeleportManager manager;
    
    private static TeleportsModule instance;
    
    public TeleportsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        manager = new TeleportManager();
        Bukkit.getPluginManager().registerEvents(manager, getPlugin());
        Global.getRemoteManager().registerInterest("teleports", TeleportActions.class);
        teleports = Global.getRemoteManager().getRemote(TeleportActions.class);
        
        return true;
    }
    
    @Override
    public boolean onEnable() throws Exception {
        Bukkit.getPluginManager().registerEvents(manager, getPlugin());
        
        instance = this;
        return true;
    }
    
    @Override
    public void onDisable(DisableReason reason) throws Exception {
        instance = null;
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new TeleportCommands(teleports), getPlugin());
    }
    
    public static TeleportManager getTeleportManager() {
        Preconditions.checkState(instance != null, "TeleportsModule is not enabled");
        
        return instance.manager;
    }
}
