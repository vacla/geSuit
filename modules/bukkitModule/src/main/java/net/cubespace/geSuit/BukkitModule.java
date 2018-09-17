package net.cubespace.geSuit;

import net.cubespace.geSuit.task.PluginMessageTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayOutputStream;

/**
 * Created for the AddstarMC Project. Created by Narimm on 12/09/2018.
 */
public abstract class BukkitModule extends JavaPlugin {
    public static BukkitModule instance;
    private String CHANNEL_NAME;
    private boolean legacy;
    private boolean isSender;
    
    /**
     *
     * @param key the channelname key
     * @param isSender if true will register outgoing plugin channels as define by the key.
     */
    protected BukkitModule(String key, boolean isSender){
        setChannelName(key);
        legacy = !(this.getServer().getVersion().contains("1.13"));
        this.isSender = isSender;
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
        if(CHANNEL_NAME.length() >20){
            this.getServer().getLogger().warning(this.getName() + " tried to registed channel " +
                    "with a length over 20...unsupported...sending disabled");
        }
        
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
        if(isSender)registerChannels();
        registerCommands();
        registerListeners();
        StringBuilder message = new StringBuilder();
        message.append(System.getProperty("line.separator"));
        message.append(this.getName()).append(" registered the ")
                .append("following outgoing channels: ");
        for (String name : this.getServer().getMessenger().getOutgoingChannels(this)){
                message.append(name).append(", ");
        }
        message.append(System.getProperty("line.separator"));
        message.append(" and the following incoming channels");
        for (String name : this.getServer().getMessenger().getIncomingChannels(this)){
            message.append(name).append(", ");
        }
        this.getServer().getLogger().config(message.toString());
    }
    
    @Override
    public void onDisable(){
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        super.onDisable();
    }
    
    protected void registerChannels(){
        registerOutgoingChannel();
    }
    protected void registerPluginMessageListener(JavaPlugin plugin, PluginMessageListener listener){
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin,
                getCHANNEL_NAME(), listener);
    }
    
    protected   void registerOutgoingChannel(){
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, getCHANNEL_NAME());
    }
    
    protected abstract void registerCommands();
    
    protected void registerListeners(){}
    
    public void sendMessage(ByteArrayOutputStream b){
        new PluginMessageTask(b,this).runTaskAsynchronously(this);
    }
}
