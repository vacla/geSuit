package net.cubespace.geSuit.modules;

import net.cubespace.geSuit.CommandManager;
import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.channel.ChannelManager;

/**
 * Represents a module.
 * Subclasses of this should add the {@link Module} annotation to provide a name for this module
  */
public abstract class BaseModule {
    private GSPlugin plugin;
    
    public BaseModule(GSPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * @return The name of this module (as specified in the {@link Module} annotation, or the class name).
     */
    public final String getName() {
        Module tag = getClass().getAnnotation(Module.class);
        if (tag != null) {
            return tag.name();
        }
        
        return getClass().getSimpleName();
    }
    
    /**
     * Called upon loading of this module. This is called upon startup only. Reloading will <b>not</b> call this.
     * @return True if everything loaded successfully.
     * @throws Exception Any thrown exception will mean a load failure and this module will not be enabled. onDisable will not be called if this occurs.
     */
    public boolean onLoad() throws Exception { return true; }
    
    /**
     * Called upon enabling of this module. This will be called upon startup and any successive reloads 
     * @return True if everything enabled successfully
     * @throws Exception Any thrown exception will mean an enable failure and this module will be disabled (through onDisable() with the reason 'Failure')
     */
    public boolean onEnable() throws Exception { return true; }
    
    /**
     * Called upon disabling of this module. This must clear up any resources that have been opened by this module. If the reason is 'Reload' you may leave some resources open if you want.
     * The 'Failure' reason should be treated in much the same was as a Shutdown, clearing any resources that may be open. 
     * @param reason The reason for this disable.
     * @throws Exception Just a convenience to make code neater. Exceptions are handled with a stack trace printout
     */
    public void onDisable(DisableReason reason) throws Exception {}
    
    /**
     * Called when this modules commands are ready to be registered (if any)
     * @param manager The CommandManager to register commands in
     */
    public void registerCommands(CommandManager manager) {}
    
    /**
     * Called upon a request to reload configurations
     */
    public void onReloadConfiguration() {}
    
    public final GSPlugin getPlugin() {
        return plugin;
    }
    
    public final ChannelManager getChannelManager() {
        return plugin.getChannelManager();
    }
    
    public enum DisableReason {
        Shutdown,
        Failure,
        Reload
    }
}
