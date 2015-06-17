package net.cubespace.geSuit.core.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.logging.Level;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.messages.LangUpdateMessage;
import net.cubespace.geSuit.core.util.Utilities;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

/**
 * This class provides an interface for loading and using language files.
 * On BungeeCord, the language defaults will be loaded at startup. 
 * On Spigot, the language defaults are combined with the current language
 * spec with the current spec overriding defaults. As such, {@link #getDefaultLang()}
 * is not available on spigot
 */
public class Messages {
    private Properties defaults;
    private Properties messages;
    
    public Messages() {
    }
    
    /**
     * Loads the current language value from the update packet
     * @param updatePacket The packet to load from
     */
    public void load(LangUpdateMessage updatePacket) {
        defaults = null;
        messages = updatePacket.messages;
    }

    /**
     * Loads the default language values from the en_US lang file in the jar. 
     * This is only to be called on the Proxy side and only by geSuit 
     */
    public void loadDefaults() {
        defaults = new Properties();
        try {
            InputStream in = Messages.class.getResourceAsStream("/lang/en_US.lang");
            defaults.load(in);
        } catch (IOException e) {
            Global.getPlatform().getLogger().log(Level.SEVERE, "Failed to load language defaults: ", e);
        }
        
        messages = new Properties(defaults);
    }
    
    /**
     * Loads messages from an input stream. The stream will be loaded as UTF-8.
     * Use {@link #load(Reader)} to use a different encoding
     * @param in The stream to load form
     * @throws IOException Thrown if an IOException occurs
     */
    public void load(InputStream in) throws IOException {
        messages = new Properties(defaults);
        InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8);
        messages.load(reader);
    }
    
    /**
     * Loads messages from a reader.
     * @param reader The reader to load from
     * @throws IOException Thrown if an IOException occurs
     */
    public void load(Reader reader) throws IOException {
        messages = new Properties(defaults);
        messages.load(reader);
    }
    
    /**
     * Loads messages from a file. The file will be loaded as UTF-8. 
     * Use {@link #load(Reader)} to use a different encoding.
     * @param file The file to load
     * @throws IOException Thrown if an IOException occurs
     */
    public void load(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8);
            load(reader);
        } finally {
            Closeables.closeQuietly(in);
        }
    }
    
    /**
     * Gets the translation of a key with formatting applied.
     * This version of the method does not support tokens
     * @param id The id to translate
     * @return The translated string or the id in braces if it didnt exist
     */
    public String get(String id) {
        String message = messages.getProperty(id);
        if (message != null) {
            return Utilities.colorize(message);
        } else {
            return String.format("{%s}", id);
        }
    }
    
    /**
     * Gets the translation of a key with formatting applied.
     * @param id The id to translate
     * @param values The tokens and replacement values.
     *              This should be an array of 
     *              {@code [String token1, Object value1, String token2, Object value2, ...]} 
     * @return The translated string or the id in braces if it didnt exist
     */
    public String get(String id, Object... values) {
        String message = messages.getProperty(id);
        if (message != null) {
            for (int i = 0; i < values.length - 1; i += 2) {
                message = message.replace(String.format("{%s}", values[i]), String.valueOf(values[i+1]));
            }
            
            return Utilities.colorize(message);
        } else {
            return String.format("{%s}", id);
        }
    }
    
    /**
     * @return Returns the default language file. This WILL be null on spigot side.
     */
    public Properties getDefaultLang() {
        return defaults;
    }
    
    /**
     * @return Returns the current language file.
     */
    public Properties getLang() {
        return messages;
    }
}
