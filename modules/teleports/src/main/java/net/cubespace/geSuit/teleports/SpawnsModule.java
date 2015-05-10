package net.cubespace.geSuit.teleports;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.teleports.commands.SpawnCommands;

public class SpawnsModule extends BaseModule {
    public SpawnsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new SpawnCommands(), getPlugin());
    }
}
