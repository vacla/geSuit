package net.cubespace.geSuit;

import net.cubespace.geSuit.task.PluginMessageTask;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.ByteArrayOutputStream;

/**
 * Created for the AddstarMC Project. Created by Narimm on 12/09/2018.
 */
public abstract class BukkitModule extends JavaPlugin {
    public static BukkitModule instance;
    private String CHANNEL_NAME;
    private boolean legacy;
    
    protected BukkitModule(String key){
        setChannelName(key);
        legacy = !(this.getServer().getVersion().contains("1.13"));
    }
    
    public boolean isLegacy(){
        return legacy;
    }
    
    /**
     * A unique key for this plugin it will be namespace with gesuit;
     * @param key
     */
    private void setChannelName(String key){
        CHANNEL_NAME = "gesuit:"+key;
    }
    
    /**
     * @return String - the full namespaced channel name
     */
    public String getCHANNEL_NAME(){
        return CHANNEL_NAME;
    }
    
    public static BukkitModule getInstance(){
        return instance;
    }
    @Override
    public void onEnable(){
        super.onEnable();
        instance = this;
        registerChannels();
        registerCommands();
        registerListeners();
    }
    
    @Override
    public void onDisable(){
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        super.onDisable();
    }
    
    protected abstract void registerChannels();
    
    protected abstract void registerCommands();
    
    protected void registerListeners(){}
    
    public void sendMessage(ByteArrayOutputStream b){
        new PluginMessageTask(b,this).runTaskAsynchronously(this);
    }
}
