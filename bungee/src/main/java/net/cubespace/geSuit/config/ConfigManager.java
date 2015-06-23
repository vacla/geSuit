package net.cubespace.geSuit.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.geSuitPlugin;

public class ConfigManager {
    private geSuitPlugin plugin;
    private boolean hasInitialized;
    
    private Set<ConfigReloadListener> listeners;
    
    // Configs
    private MainConfig mainConfig;
    private ModerationConfig moderationConfig;
    private TeleportsConfig teleportsConfig;
    private AnnouncementsConfig announcementsConfig;
    
    // MOTDs
    private MOTDFile motd;
    private MOTDFile motdNew;
    
    public ConfigManager(geSuitPlugin plugin, File base) {
        this.plugin = plugin;
        
        listeners = Sets.newConcurrentHashSet();
        
        mainConfig = new MainConfig(new File(base, "config.yml"));
        moderationConfig = new ModerationConfig(new File(base, "moderation.yml"));
        teleportsConfig = new TeleportsConfig(new File(base, "teleports.yml"));
        announcementsConfig = new AnnouncementsConfig(new File(base, "announcements.yml"));
        
        motd = new MOTDFile(new File(base, "motd.txt"));
        motdNew = new MOTDFile(new File(base, "motd-new.txt"));
    }
    
    /**
     * Initializes each configuration by creating them if not present
     * and loading all present configs. This must only be called once.
     * Any wish to reload configurations must be done through {@link #reloadAll()}
     * @throws InvalidConfigurationException Thrown if a configuration contains an error
     * @throws IOException 
     */
    public void initialize() throws InvalidConfigurationException, IOException {
        Preconditions.checkState(!hasInitialized);
        
        mainConfig.init();
        moderationConfig.init();
        teleportsConfig.init();
        announcementsConfig.init();
        
        if (!motd.getFile().exists()) {
            saveDefaultFile(motd.getFile().getName(), motd.getFile());
        }
        if (!motdNew.getFile().exists()) {
            saveDefaultFile(motdNew.getFile().getName(), motdNew.getFile());
        }
        motd.load();
        motdNew.load();
        
        hasInitialized = true;
    }
    
    /**
     * Reloads each configuration.
     * @throws InvalidConfigurationException Thrown if a configuration contains an error
     */
    public void reloadAll() throws InvalidConfigurationException {
        mainConfig.load();
        moderationConfig.load();
        teleportsConfig.load();
        announcementsConfig.load();
        
        try {
            motd.load();
            motdNew.load();
        } catch (IOException e) {
            throw new InvalidConfigurationException("Unable to access motd file", e);
        }
        
        for (ConfigReloadListener listener : listeners) {
            listener.onConfigReloaded(this);
        }
    }
    
    private void saveDefaultFile(String path, File destination) throws IOException {
        InputStream stream = plugin.getResourceAsStream(path);
        Preconditions.checkNotNull(stream, "path does not exist");
        
        Files.copy(stream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Adds a reload listener.
     * Does nothing if already registered
     * @param listener The listener to register
     */
    public void addReloadListener(ConfigReloadListener listener) {
        Preconditions.checkNotNull(listener);
        listeners.add(listener);
    }
    
    /**
     * Removes a previously added listener.
     * Does nothing if not registered.
     * @param listener The listener to remove
     */
    public void removeReloadListener(ConfigReloadListener listener) {
        Preconditions.checkNotNull(listener);
        listeners.remove(listener);
    }
    
    /**
     * @return Returns the main configuration
     */
    public MainConfig config() {
        return mainConfig;
    }
    
    /**
     * @return Returns the configuration for the moderation module
     */
    public ModerationConfig moderation() {
        return moderationConfig;
    }
    
    /**
     * @return Returns the configuration for the teleports module
     */
    public TeleportsConfig teleports() {
        return teleportsConfig;
    }
    
    /**
     * @return Returns the configuration for announcements
     */
    public AnnouncementsConfig announcements() {
        return announcementsConfig;
    }
    
    /**
     * Gets the MOTD for either new or existing players
     * @param newPlayer When true, the new player MOTD is used
     * @return The MOTD or an empty string
     */
    public String getMOTD(boolean newPlayer) {
        if (newPlayer) {
            return motdNew.getMOTD();
        } else {
            return motd.getMOTD();
        }
    }
}
