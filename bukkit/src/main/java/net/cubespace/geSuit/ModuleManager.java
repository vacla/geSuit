package net.cubespace.geSuit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.modules.Module;
import net.cubespace.geSuit.modules.BaseModule.DisableReason;

public class ModuleManager implements Listener {
    private GSPlugin plugin;
    private boolean loaded;
    
    private Map<String, BaseModule> loadedModules;
    private Map<BaseModule, ModuleDefinition> moduleRevDefs;
    private Map<String, ModuleDefinition> modules;
    
    ModuleManager(GSPlugin plugin) {
        this.plugin = plugin;
        
        loaded = false;
        loadedModules = Maps.newHashMap();
        modules = Maps.newHashMap();
        moduleRevDefs = Maps.newIdentityHashMap();
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private String last(String[] strings) {
        return strings[strings.length-1];
    }
    
    public boolean registerModule(String moduleClass, String... dependencies) {
        
        String moduleName = last(moduleClass.split("\\.|\\$"));
        
        // Attempt to load the class so we can get more info. If this module has deps, 
        // this will fail if any the class signature uses classes not available yet
        try {
            Class<?> loadedClass = Class.forName(moduleClass);
            String newName = getModuleInformation(loadedClass);
            if (newName != null) {
                moduleName = newName;
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("Plugin attempted to register module with non-existant class");
            return false;
        } catch (ExceptionInInitializerError e) {
            // Ignore
        }
        
        // Register it
        return registerModule0(moduleClass, moduleName, dependencies);
    }
    
    public boolean registerModule(Class<? extends BaseModule> moduleClass, String... dependencies) {
        String moduleName = getModuleInformation(moduleClass);
        
        if (moduleName == null) {
            moduleName = moduleClass.getSimpleName();
        }
        
        return registerModule0(moduleClass.getName(), moduleName, dependencies);
    }
    
    private boolean registerModule0(String moduleClass, String name, String[] dependencies) {
        if (modules.containsKey(name.toLowerCase())) {
            return false;
        }
        
        ModuleDefinition def = new ModuleDefinition(moduleClass, name, dependencies);
        modules.put(name.toLowerCase(), def);
        
        // Automatically load the module if we are past the load time
        if (loaded)
            loadSingleModule(def);
        
        return true;
    }
    
    private String getModuleInformation(Class<?> moduleClass) {
        Module tag = moduleClass.getAnnotation(Module.class);
        
        if (tag == null) {
            return null;
        }
        
        return tag.name();
    }
    
    private boolean checkDependencies(String[] dependencies) {
        for (String pluginName : dependencies) {
            if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                return false;
            }
        }
        
        return true;
    }
    
    public ModuleState getModuleState(BaseModule module) {
        return moduleRevDefs.get(module).state;
    }
    
    private void logModule(Level level, String message) {
        plugin.getLogger().log(level, String.format("[Modules] %s", message));
    }
    
    private void logModule(Level level, String module, String message) {
        plugin.getLogger().log(level, String.format("[%s] %s", module, message));
    }
    
    private void logModule(Level level, String module, String message, Throwable error) {
        plugin.getLogger().log(level, String.format("[%s] %s", module, message), error);
    }
    
    void loadAll() {
        Preconditions.checkState(!loaded, "All modules have already been loaded.");
        
        loaded = true;
        List<String> missingDependencies = Lists.newArrayList();
        Map<String, String> toChange = Maps.newHashMap();
        
        for (ModuleDefinition def : modules.values()) {
            missingDependencies.clear();
            
            // Check dependencies
            for (String pluginName : def.dependencies) {
                if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
                    missingDependencies.add(pluginName);
                }
            }
            
            if (!missingDependencies.isEmpty()) {
                logModule(Level.INFO, String.format("Not loading module %s, missing dependencies %s", def.name, missingDependencies));
                continue;
            }
            
            String oldName = def.name;
            if (!loadModule(def)) {
                // loadModule should log the reason
                continue;
            }
            
            // Update name
            if (!def.name.equalsIgnoreCase(oldName)) {
                if (!modules.containsKey(def.name.toLowerCase())) {
                    toChange.put(oldName, def.name);
                } else {
                    logModule(Level.WARNING, String.format("Module %s will not use the name %s and will keep its temporary name due to a name conflict", oldName, def.name));
                    def.name = oldName;
                }
            }
            
            logModule(Level.INFO, def.name, "Loaded successfully");
        }
        
        // Do name updates
        for (Entry<String, String> entry : toChange.entrySet()) {
            ModuleDefinition def = modules.remove(entry.getKey().toLowerCase());
            BaseModule module = loadedModules.remove(entry.getKey().toLowerCase());
            
            modules.put(entry.getValue().toLowerCase(), def);
            loadedModules.put(entry.getValue().toLowerCase(), module);
        }
    }
    
    private boolean loadSingleModule(ModuleDefinition def) {
        String oldName = def.name;
        if (!loadModule(def)) {
            // loadModule should log the reason
            return false;
        }
        
        // Update name
        if (!def.name.equalsIgnoreCase(oldName)) {
            if (!modules.containsKey(def.name.toLowerCase())) {
                modules.remove(oldName.toLowerCase());
                BaseModule module = loadedModules.remove(oldName.toLowerCase());
                
                modules.put(def.name.toLowerCase(), def);
                loadedModules.put(def.name.toLowerCase(), module);
            } else {
                logModule(Level.WARNING, String.format("Module %s will not use the name %s and will keep its temporary name due to a name conflict", oldName, def.name));
                def.name = oldName;
            }
        }
        
        logModule(Level.INFO, def.name, "Loaded successfully");
        return true;
    }
    
    private boolean loadModule(ModuleDefinition def) {
        try {
            Class<?> rawClass = Class.forName(def.className);
            // Make sure its a module type
            if (!BaseModule.class.isAssignableFrom(rawClass)) {
                logModule(Level.SEVERE, String.format("Error loading module %s, class is not a subclass of BaseModule"));
                return false;
            }
            
            // Construct it
            Constructor<? extends BaseModule> constructor = rawClass.asSubclass(BaseModule.class).getDeclaredConstructor(GSPlugin.class);
            constructor.setAccessible(true);
            BaseModule module = constructor.newInstance(plugin);
            
            // Load it
            if (!module.onLoad()) {
                logModule(Level.SEVERE, String.format("Error loading module %s, class is not a subclass of BaseModule"));
                def.state = ModuleState.LoadFailure;
                return false;
            } else {
                def.state = ModuleState.Loaded;
            }
            
            // Register commands
            module.registerCommands(plugin.getCommandManager());
            
            loadedModules.put(def.name.toLowerCase(), module);
            moduleRevDefs.put(module, def);
            
            // Update name
            String newName = getModuleInformation(rawClass);
            if (newName != null && !newName.equals(def.name)) {
                def.name = newName;
            }
            
            return true;
        } catch (ClassNotFoundException e) {
            logModule(Level.SEVERE, def.name, String.format("Error: Unable to find class %s", e.getMessage()));
            return false;
        } catch (ExceptionInInitializerError e) {
            logModule(Level.SEVERE, def.name, "Error: Unable to initialize class", e);
            return false;
        } catch (NoSuchMethodException e) {
            logModule(Level.SEVERE, def.name, "Error: Module does not have a constructor which takes one GSPlugin object");
            return false;
        } catch (InstantiationException e) {
            logModule(Level.SEVERE, def.name, "Error: An error occured while instantiating the module class", e);
            return false;
        } catch (IllegalAccessException e) {
            logModule(Level.SEVERE, def.name, "Error: Unable to access the modules constructor");
            return false;
        } catch (InvocationTargetException e) {
            logModule(Level.SEVERE, def.name, "Error: An error occured while instantiating the module class", e.getCause());
            return false;
        } catch (Exception e) {
            logModule(Level.SEVERE, def.name, "An error occured while loading the module:", e);
            return false;
        }
    }
    
    void enableAll() {
        for (BaseModule module : loadedModules.values()) {
            if (getModuleState(module) == ModuleState.Enabled) {
                continue;
            }
            
            enableModule(module);
        }
    }
    
    private void enableModule(BaseModule module) {
        try {
            if (!module.onEnable()) {
                logModule(Level.WARNING, module.getName(), "Failed to enable module, disabling");
                disableModule(module, DisableReason.Failure);
                return;
            }
            
            if (module instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener)module, plugin);
            }
            
            moduleRevDefs.get(module).state = ModuleState.Enabled;
            
            logModule(Level.INFO, module.getName(), "Enabled successfully");
        } catch (Exception e) {
            logModule(Level.SEVERE, module.getName(), "An error occured while enabling the module:", e);
            disableModule(module, DisableReason.Failure);
        }
    }
    
    void disableAll() {
        for (BaseModule module : loadedModules.values()) {
            if (getModuleState(module) == ModuleState.Enabled) {
                disableModule(module, DisableReason.Shutdown);
            }
        }
    }
    
    private void disableModule(BaseModule module, DisableReason reason) {
        try {
            module.onDisable(reason);
            
            if (module instanceof Listener) {
                HandlerList.unregisterAll((Listener)module);
            }
            
            moduleRevDefs.get(module).state = (reason == DisableReason.Failure ? ModuleState.Failure : ModuleState.Loaded);
        } catch (Exception e) {
            logModule(Level.SEVERE, module.getName(), "An error occured while disabling this module:", e);
        }
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    private void onPluginLoad(PluginEnableEvent event) {
        for (ModuleDefinition def : modules.values()) {
            if (def.state != ModuleState.Unloaded) {
                continue;
            }
            
            if (checkDependencies(def.dependencies)) {
                // YAY we can load it
                loadSingleModule(def);
            }
        }
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    private void onPluginUnload(PluginDisableEvent event) {
        Iterator<BaseModule> it = loadedModules.values().iterator();
        
        while(it.hasNext()) {
            BaseModule module = it.next();
            ModuleDefinition def = moduleRevDefs.get(module);
            
            // Check dependencies
            boolean unload = false;
            for (String pluginName : def.dependencies) {
                if (event.getPlugin().getName().equalsIgnoreCase(pluginName)) {
                    unload = true;
                    break;
                }
            }
            
            if (!unload) {
                continue;
            }
            
            // Disable the module first
            if (def.state == ModuleState.Enabled) {
                disableModule(module, DisableReason.Shutdown);
            }
            
            // Unload the module
            it.remove();
            def.state = ModuleState.Unloaded;
            logModule(Level.INFO, def.name, String.format("Unloaded module because dependency %s was disabled", event.getPlugin().getName()));
        }
    }
    
    private static class ModuleDefinition {
        public String className;
        public String name;
        public String[] dependencies;
        public ModuleState state;
        
        public ModuleDefinition(String className, String name, String[] dependencies) {
            this.className = className;
            this.name = name;
            this.dependencies = dependencies;
            
            state = ModuleState.Unloaded;
        }
    }
    
    public enum ModuleState {
        Unloaded, // Not in memory
        LoadFailure, // Not in memory, load failed
        Loaded, // In memory, but not enabled
        Enabled, // In memory and enabled
        Failure // In memory and loaded, but failed to enable
    }
}
