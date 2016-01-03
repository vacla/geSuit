package net.cubespace.geSuit.config;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class BroadcastsConfig extends Config {
    public BroadcastsConfig(File file) {
        CONFIG_FILE = file;
        CONFIG_HEADER = new String[] {
                "You can define broadcasts to run periodically here. 'Global' can be used to",
                "define broadcasts that are shown to all users online. There is an exception",
                "to this described below",
                "",
                "'Servers' can be used to define broadcasts that are per server. Within Servers",
                "the broadcast definitions are to be keyed by the server name. The definition",
                "for each server entry uses the same format as the Global entry, allowing for",
                "customization of the Messages and Interval for each server",
                "",
                "Broadcast Section format",
                "The format for broadcasts is as follows:",
                " Interval: <time> # eg 5m",
                " RandomStart: <true|false> # When true a random message is selected to be the first displayed",
                " Isolated: <true|false> # When true, the messages defined in 'Global' will not be shown on this server.",
                " Messages: [] # The messages to be shown. Remember you can use '| ' as a prefix to use multiple lines",
                "",
                "'Named' can be used to create named messages that can be broadcasted via command or automatically.",
                "The expected format is '<name>: <message>'. No extra features are present for named broadcasts",
                "To use these in automated broadcasts simply use '@<name>' as the message. You WILL need to quote it",
                "otherwise a parse error will occur",
                "",
                "All messages support minecraft color codes using &",
                "After making changes, reload with command gsreload"
        };
    }
    
    @Comment("When true, automated broadcasts will be enabled. Manual broadcasts will always be available")
    public Boolean Enabled = true;
    @Comment("The cooldown time for manual broadcasts with /!announce")
    public String ManualCooldown = "30s";
    @Comment("The global broadcast settings. NOTE: Isolated property will be ignored")
    public BroadcastEntry Global = new BroadcastEntry();
    public HashMap<String, BroadcastEntry> Servers = new HashMap<>();
    public HashMap<String, String> Named = new HashMap<>();
    
    public static class BroadcastEntry extends Config {
        public String Interval = "5m";
        public Boolean RandomStart = false;
        public Boolean Isolated = false;
        public ArrayList<String> Messages = new ArrayList<>();
    }
}


