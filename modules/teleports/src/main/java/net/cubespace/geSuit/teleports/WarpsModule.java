package net.cubespace.geSuit.teleports;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.teleports.commands.WarpCommands;
import net.cubespace.geSuit.teleports.warps.WarpManager;

public class WarpsModule extends BaseModule {
    private WarpManager manager;
    
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
        return true;
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new WarpCommands(this.manager), getPlugin());
    }
}
