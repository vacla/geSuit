package net.cubespace.geSuit.teleports;

import com.google.common.base.Preconditions;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.teleports.commands.WarpCommands;
import net.cubespace.geSuit.teleports.warps.WarpManager;

public class WarpsModule extends BaseModule {
    private WarpManager manager;
    private static WarpsModule instance;
    
    public WarpsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        manager = new WarpManager();
        return true;
    }
    
    @Override
    public boolean onEnable() throws Exception {
        manager.loadWarps();
        instance = this;
        return true;
    }
    
    @Override
    public void onDisable(DisableReason reason) throws Exception {
        instance = null;
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new WarpCommands(this.manager), getPlugin());
    }
    
    public static WarpManager getWarpManager() {
        Preconditions.checkState(instance != null, "WarpsModule is not enabled");
        
        return instance.manager;
    }
}
