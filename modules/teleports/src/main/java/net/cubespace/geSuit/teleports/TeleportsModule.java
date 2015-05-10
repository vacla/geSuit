package net.cubespace.geSuit.teleports;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.teleports.commands.TeleportCommands;

public class TeleportsModule extends BaseModule {
    public TeleportsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new TeleportCommands(), getPlugin());
    }
}
