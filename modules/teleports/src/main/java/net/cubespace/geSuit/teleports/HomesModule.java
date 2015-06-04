package net.cubespace.geSuit.teleports;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.teleports.commands.HomeCommands;
import net.cubespace.geSuit.teleports.homes.HomeManager;

public class HomesModule extends BaseModule {
    private HomeManager manager;
    
    public HomesModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        manager = new HomeManager();
        return true;
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new HomeCommands(this.manager), getPlugin());
    }
}
