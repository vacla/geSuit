package net.cubespace.geSuit.portals;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.storage.StorageInterface;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.portals.commands.PortalCommands;

public class PortalsModule extends BaseModule {
    private PortalManager manager;
    private WorldEditPlugin worldEdit;
    
    private PortalListener listener;
    
    public PortalsModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        Channel<BaseMessage> channel = Global.getChannelManager().createChannel("portals", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        
        StorageInterface section = Global.getStorageProvider().create("geSuit.portals");
        
        manager = new PortalManager(channel, section);
        worldEdit = JavaPlugin.getPlugin(WorldEditPlugin.class);
        
        return true;
    }
    
    @Override
    public boolean onEnable() throws Exception {
        manager.setCurrentServer(Global.getServer());
        manager.loadPortals();
        listener = new PortalListener(manager);
        Bukkit.getPluginManager().registerEvents(listener, getPlugin());
        
        return true;
    }
    
    @Override
    public void onDisable(DisableReason reason) throws Exception {
        HandlerList.unregisterAll(listener);
        manager.clearPortals();
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new PortalCommands(this.manager,  this.worldEdit), getPlugin());
    }
}
