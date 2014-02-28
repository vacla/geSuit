package net.cubespace.getSuit.configs;

import net.cubespace.getSuit.BungeeSuite;
import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.ConfigMode;

import java.io.File;

public class Messages extends Config {
    public Messages() {
        CONFIG_FILE = new File(BungeeSuite.instance.getDataFolder(), "Messages.yml");
        CONFIG_MODE = ConfigMode.FIELD_IS_KEY;
    }

    public String MOTD = "§dWelcome to the server {player}!";
    public String PLAYER_CONNECT_PROXY = "{player}§e has joined the server!";
    public String PLAYER_DISCONNECT_PROXY = "{player}§e has left the server!";
    public String PLAYER_DOES_NOT_EXIST = "§c" + "That player does not exist";
    public String PLAYER_LOAD = "Loaded player §9{player}";
    public String PLAYER_UNLOAD = "Unloaded player §c{player}";
    public String PLAYER_NOT_ONLINE = "§c" + "That player is not online";
    public String NO_PERMISSION = "§c" + "You do not have permission to use that command";
    public String NEW_PLAYER_BROADCAST = "§d Welcome {player} the newest member of the server!";

    // teleport specific messages
    public String ALL_PLAYERS_TELEPORTED = "§2" + "All players have been teleported to {player}";
    public String TELEPORTED_TO_PLAYER = "§2" + "You have been teleported to {player}";
    public String PLAYER_TELEPORT_PENDING = "§c" + "You already have a teleport pending";
    public String PLAYER_TELEPORT_PENDING_OTHER = "§c" + "That player already has a teleport pending";
    public String PLAYER_TELEPORTED_TO_YOU = "§2" + "{player} has teleported to you";
    public String PLAYER_TELEPORTED = "§2" + "{player} has teleported to {target}";
    public String PLAYER_REQUESTS_TO_TELEPORT_TO_YOU = "§2" + "{player} has requested to teleport to you. Type /tpaccept to allow";
    public String PLAYER_REQUESTS_YOU_TELEPORT_TO_THEM = "§2" + "{player} has requested you teleport to them. Type /tpaccept to allow";
    public String TELEPORT_DENIED = "§c" + "You denied {player}'s teleport request";
    public String TELEPORT_REQUEST_DENIED = "§c" + "{player} denied your teleport request";
    public String NO_TELEPORTS = "§c" + "You do not have any pending teleports";
    public String TELEPORT_REQUEST_SENT = "§2" + "Your request has been sent";
    public String TPA_REQUEST_TIMED_OUT = "§c" + "Your request to teleport to {player} has timed out";
    public String TP_REQUEST_OTHER_TIMED_OUT = "§c" + "{player}'s teleport request has timed out";
    public String TPAHERE_REQUEST_TIMED_OUT = "§c" + "Your request to have {player} teleport to you has timed out";
    public String NO_BACK_TP = "§c" + "You do not have anywhere to go back to";
    public String SENT_BACK = "§2" + "You have been sent back";
    public String TELEPORT_TOGGLE_ON = "§2" + "Telports have been toggled on";
    public String TELEPORT_TOGGLE_OFF = "§c" + "Telports have been toggled off";
    public String TELEPORT_UNABLE = "§c" + "You are unable to teleport to this player";
    // warp specific messages
    public String WARP_CREATED = "§2" + "Successfully created a warp";
    public String WARP_UPDATED = "§2" + "Successfully updated the warp";
    public String WARP_DELETED = "§2" + "Successfully deleted the warp";
    public String PLAYER_WARPED = "§7" + "You have been warped to {warp}";
    public String PLAYER_WARPED_OTHER = "§7" + "{player} has been warped to {warp}";
    public String WARP_DOES_NOT_EXIST = "§c" + "That warp does not exist";
    public String WARP_NO_PERMISSION = "§c" + "You do not have permission to use that warp";
    public String WARP_SERVER = "§c" + "Warp not on the same server";

    // portal specific messages
    public String PORTAL_NO_PERMISSION = "§c" + "You do not have permission to enter this portal";
    public String PORTAL_CREATED = "§2" + "You have successfully created a portal";
    public String PORTAL_UPDATED = "§2" + "You have successfully updated the portal";
    public String PORTAL_DELETED = "§c" + "Portal deleted";
    public String PORTAL_FILLTYPE = "§c" + "That filltype does not exist";
    public String PORTAL_DESTINATION_NOT_EXIST = "§c" + "That portal destination does not exist";
    public String PORTAL_DOES_NOT_EXIST = "§c" + "That portal does not exist";
    public String INVALID_PORTAL_TYPE = "§c" + "That is an invalid portal type. Use warp or server";
    public String NO_SELECTION_MADE = "§c" + "No world edit selection has been made";
    // Spawn messages
    public String SPAWN_DOES_NOT_EXIST = "§c" + "The spawn point has not been set yet";
    public String SPAWN_UPDATED = "§2" + "Spawn point updated";
    public String SPAWN_SET = "§2" + "Spawn point set";
    // ban messages
    public String KICK_PLAYER_MESSAGE = "§c" + "You have been kicked for: {message}, by {sender}";
    public String KICK_PLAYER_BROADCAST = "§c" + "{player} has been kicked for {message}, by {sender}!";
    public String PLAYER_ALREADY_BANNED = "§c" + "That player is already banned";
    public String PLAYER_NOT_BANNED = "§c" + "That player is not banned";
    public String IPBAN_PLAYER = "§c" + "You have been IP banned for: {message}, by {sender}";
    public String IPBAN_PLAYER_BROADCAST = "§c" + "{player} has been ip banned for: {message}, by {sender}";
    public String DEFAULT_BAN_REASON = "Breaking server rules";
    public String DEFAULT_KICK_MESSAGE = "§cBreaking server rules";
    public String BAN_PLAYER_MESSAGE = "§c" + "You have been banned for: {message}, by {sender}";
    public String BAN_PLAYER_BROADCAST = "§c" + "{player} has been banned for: {message}, by {sender}";
    public String TEMP_BAN_BROADCAST = "§c" + "{player} has been temporarily banned for {message} until {time}, by {sender}!";
    public String TEMP_BAN_MESSAGE = "§c" + "You have been temporarily until {time} for {message}, by {sender}!";
    public String PLAYER_UNBANNED = "§2" + "{player} has been unbanned by {sender}!";

    public String SENT_HOME = "§2" + "You have been sent home";
    public String NO_HOMES_ALLOWED_SERVER = "§c" + "Your are not able to set anymore homes on this server";
    public String NO_HOMES_ALLOWED_GLOBAL = "§c" + "Your are not able to set anymore homes globally";
    public String NO_HOMES = "§c" + "You do not have any set homes";

    public String HOME_UPDATED = "§2" + "Your home has been updated";
    public String HOME_SET = "§2" + "Your home has been set";
    public String HOME_DOES_NOT_EXIST = "§c" + "That home does not exist";
    public String HOME_DELETED = "§c" + "Your home has been deleted";
}
