package net.cubespace.geSuit.teleports;

import com.google.common.base.Preconditions;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.teleports.commands.SpawnCommands;
import net.cubespace.geSuit.teleports.spawns.SpawnManager;

public class SpawnsModule extends BaseModule {
    private SpawnManager manager;
    private static SpawnsModule instance;
    
    public SpawnsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        manager = new SpawnManager();
        return true;
    }
    
    @Override
    public boolean onEnable() throws Exception {
        manager.loadSpawns();
        
        instance = this;
        return true;
    }
    
    @Override
    public void onDisable(DisableReason reason) throws Exception {
        instance = null;
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new SpawnCommands(this.manager), getPlugin());
    }
    
    public static SpawnManager getSpawnManager() {
        Preconditions.checkState(instance != null, "SpawnsModule is not enabled");
        
        return instance.manager;
    }
}
