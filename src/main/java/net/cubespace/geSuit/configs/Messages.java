package net.cubespace.geSuit.configs;

import java.io.File;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.ConfigMode;
import net.cubespace.geSuit.geSuit;

public class Messages extends Config {
    public Messages() {
        CONFIG_FILE = new File(geSuit.instance.getDataFolder(), "Messages.yml");
        CONFIG_MODE = ConfigMode.FIELD_IS_KEY;
    }

    public String PLAYER_SEEN_ONLINE = "&a| Player &2{player} &ais currently online!\n&a| ip:{ip}    Server:{server}";
    public String PLAYER_SEEN_OFFLINE = "&c| Player &4{player}&c is offline. Last seen: {seen}!\n&c| ip:{ip}";
    public String BUNGEE_COMMAND_SEEN_USAGE = "&cUsage: /seen <player>";
    public String PLAYER_CONNECT_PROXY = "{player}&e has joined the server!";
    public String PLAYER_DISCONNECT_PROXY = "{player}&e has left the server!";
    public String PLAYER_DOES_NOT_EXIST = "&c" + "That player does not exist";
    public String PLAYER_LOAD = "Loaded player &9{player}&7 ({uuid})";
    public String PLAYER_LOAD_CACHED = "Loaded player from cache &9{player}&7 ({uuid})";
    public String PLAYER_CREATE = "Created player &b{player}&7 ({uuid})";
    public String PLAYER_UNLOAD = "Unloaded player &c{player}";
    public String PLAYER_NOT_ONLINE = "&c" + "That player is not online";
    public String NO_PERMISSION = "&c" + "You do not have permission to use that command";
    public String NEW_PLAYER_BROADCAST = "&eNotice to everyone: &b{player} &ahas just joined this server for the first time. Please make them feel welcome!";

    // teleport specific messages
    public String ALL_PLAYERS_TELEPORTED = "&6" + "All players have been teleported to {player}";
    public String TELEPORTED_TO_PLAYER = "&6" + "You have been teleported to {player}";
    public String PLAYER_TELEPORT_PENDING = "&c" + "You already have a teleport pending";
    public String PLAYER_TELEPORT_PENDING_OTHER = "&c" + "That player already has a teleport pending";
    public String PLAYER_TELEPORTED_TO_YOU = "&6" + "{player} has teleported to you";
    public String PLAYER_TELEPORTED = "&6" + "{player} has teleported to {target}";
    public String PLAYER_REQUESTS_TO_TELEPORT_TO_YOU = "&6" + "{player} has requested to teleport to you. Type /tpaccept to allow";
    public String PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM = "&6" + "{player} has requested you teleport to them. Type /tpaccept to allow";
    public String TELEPORT_DENIED = "&c" + "You denied {player}'s teleport request";
    public String TELEPORT_REQUEST_DENIED = "&c" + "{player} denied your teleport request";
    public String NO_TELEPORTS = "&c" + "You do not have any pending teleports";
    public String TELEPORT_REQUEST_SENT = "&6" + "Your request has been sent";
    public String TPA_REQUEST_TIMED_OUT = "&c" + "Your request to teleport to {player} has timed out";
    public String TP_REQUEST_OTHER_TIMED_OUT = "&c" + "{player}'s teleport request has timed out";
    public String TPAHERE_REQUEST_TIMED_OUT = "&c" + "Your request to have {player} teleport to you has timed out";
    public String NO_BACK_TP = "&c" + "You do not have anywhere to go back to";
    public String SENT_BACK = "&6" + "You have been sent back";
    public String TELEPORT_TOGGLE_ON = "&6" + "Telports have been toggled on";
    public String TELEPORT_TOGGLE_OFF = "&c" + "Telports have been toggled off";
    public String TELEPORT_UNABLE = "&c" + "You are unable to teleport to this player";

    // warp specific messages
    public String WARP_CREATED = "&6" + "Successfully created a warp";
    public String WARP_UPDATED = "&6" + "Successfully updated the warp";
    public String WARP_DELETED = "&6" + "Successfully deleted the warp";
    public String PLAYER_WARPED = "&7" + "You have been warped to {warp}";
    public String PLAYER_WARPED_OTHER = "&7" + "{player} has been warped to {warp}";
    public String WARP_DOES_NOT_EXIST = "&c" + "That warp does not exist";
    public String WARP_NO_PERMISSION = "&c" + "You do not have permission to use that warp";
    public String WARP_SERVER = "&c" + "Warp not on the same server";

    // portal specific messages
    public String PORTAL_NO_PERMISSION = "&c" + "You do not have permission to enter this portal";
    public String PORTAL_CREATED = "&6" + "You have successfully created a portal";
    public String PORTAL_UPDATED = "&6" + "You have successfully updated the portal";
    public String PORTAL_DELETED = "&c" + "Portal deleted";
    public String PORTAL_FILLTYPE = "&c" + "That filltype does not exist";
    public String PORTAL_DESTINATION_NOT_EXIST = "&c" + "That portal destination does not exist";
    public String PORTAL_DOES_NOT_EXIST = "&c" + "That portal does not exist";
    public String INVALID_PORTAL_TYPE = "&c" + "That is an invalid portal type. Use warp or server";
    public String NO_SELECTION_MADE = "&c" + "No world edit selection has been made";

    // Spawn messages
    public String SPAWN_DOES_NOT_EXIST = "&c" + "The spawn point has not been set yet";
    public String SPAWN_UPDATED = "&6" + "Spawn point updated";
    public String SPAWN_SET = "&6" + "Spawn point set";

    // ban messages
    public String BUNGEE_COMMAND_BAN_USAGE = "&c" + "Usage: !ban <player|ip> <reason>";
    public String BUNGEE_COMMAND_WARN_USAGE = "&c" + "Usage: !warn <player> <reason>";
    public String BUNGEE_COMMAND_TEMPBAN_USAGE = "&c" + "Usage: !tempban <player> <time> <reason>";
    public String BUNGEE_COMMAND_UNBAN_USAGE = "&c" + "Usage: !unban <player|uuid|ip>";
    public String UNKNOWN_PLAYER_STILL_BANNING = "&c" + "Player is unknown. Banning by name and &nmaybe&r&c by UUID.";
    public String UNKNOWN_PLAYER_STILL_WARNING = "&c" + "Player is unknown. Warning player by name and &nmaybe&r&c by UUID.";
    public String KICK_PLAYER_MESSAGE = "&c" + "You have been kicked. Reason: {message}";
    public String KICK_PLAYER_BROADCAST = "&b" + "{player} has been kicked. Reason: {message}";
    public String PLAYER_ALREADY_BANNED = "&c" + "That player is already banned!";
    public String PLAYER_NOT_BANNED = "&a" + "That player is not banned";
    public String PLAYER_NEVER_BANNED = "&a" + "No ban history for {player}";
    public String PLAYER_NEVER_WARNED = "&a" + "No warning history for {player}";
    public String IPBAN_PLAYER = "&c" + "Your IP has been banned. Reason: {message}";
    public String IPBAN_PLAYER_BROADCAST = "&b" + "{player} has been ip banned. Reason: {message}";
    public String DEFAULT_BAN_REASON = "Unknown";
    public String DEFAULT_KICK_MESSAGE = "&cUnknown";
    public String DEFAULT_WARN_REASON = "Unknown";
    public String BAN_PLAYER_MESSAGE = "&c" + "You have been banned. Reason: {message}";
    public String BAN_PLAYER_BROADCAST = "&b" + "{player} has been banned. Reason: {message}";
    public String TEMP_BAN_BROADCAST = "&b" + "{player} has been temporarily banned ({time}). Reason: {message}";
    public String TEMP_BAN_MESSAGE = "&c" + "You have been temporarily banned ({time}). Reason: {message}";
    public String PLAYER_UNBANNED = "&c" + "{player} has been unbanned!";
    public String WARN_PLAYER_BROADCAST = "&b" + "{player} has received a warning. Reason: {message}";

    // Home messages
    public String SENT_HOME = "&6" + "You have been sent home";
    public String NO_HOMES_ALLOWED_SERVER = "&c" + "Your are not allowed to set anymore homes on this server.";
    public String NO_HOMES_ALLOWED_GLOBAL = "&c" + "Your are not allowed to set anymore homes globally.";
    public String NO_HOMES = "&c" + "You do not have any homes set.";
    public String SHOWING_YOUR_HOMES = "&eListing your homes:";
    public String SHOWING_OTHER_HOMES = "&eListing homes of {player}:";
    public String HOMES_PREFIX_THIS_SERVER = "&a{server}: &9";
    public String HOMES_PREFIX_OTHER_SERVER = "&e{server}: &9";

    public String HOME_UPDATED = "&6" + "Your home \"{home}\" has been updated";
    public String HOME_SET = "&6" + "Your home \"{home}\" has been set";
    public String HOME_DOES_NOT_EXIST = "&c" + "That home does not exist";
    public String HOME_DELETED = "&c" + "Your home \"{home}\" has been deleted";
}
