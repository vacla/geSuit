package net.cubespace.geSuit.teleports;

import com.google.common.base.Preconditions;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.remote.teleports.TeleportActions;
import net.cubespace.geSuit.teleports.commands.TeleportCommands;
import org.bukkit.Bukkit;

import java.util.List;

public class TeleportsModule extends BaseModule {
    private TeleportActions teleports;
    private TeleportManager manager;

    public static boolean isWorldGuarded() {
        return worldGuarded;
    }

    private static boolean worldGuarded;

    public static WorldGuardPlugin getmWorldGuard() {
        Preconditions.checkNotNull(mWorldGuard, "World gaurd not available");
        return mWorldGuard;
    }

    private static WorldGuardPlugin mWorldGuard;
    private static List<String> deny_Teleport;
    private static TeleportsModule instance;
    
    public TeleportsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        manager = new TeleportManager(this.getPlugin());
        Bukkit.getPluginManager().registerEvents(manager, getPlugin());
        Global.getRemoteManager().registerInterest("teleports", TeleportActions.class);
        teleports = Global.getRemoteManager().getRemote(TeleportActions.class);
        
        return true;
    }
    
    @Override
    public boolean onEnable() throws Exception {
        Bukkit.getPluginManager().registerEvents(manager, getPlugin());
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {//check for WorldGaurd
            mWorldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
            worldGuarded = true;
            deny_Teleport = getPlugin().getConfig().getStringList("teleport.denyon.cmdlist");
        } else {
            mWorldGuard = null;
            worldGuarded = false;
            deny_Teleport = null;
        }
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
