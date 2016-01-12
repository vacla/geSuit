package net.cubespace.geSuit.managers;

import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.objects.GSPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;


/**
 * @author benjamincharlton on 26/08/2015.
 */
public class LockDownManager {

    private static boolean lockedDown = false;
    private static long expiryTime = 0;
    private static String optionalMessage = "";

    public static void initialize() {
        if (ConfigManager.lockdown.LockedDown) {
            long time = System.currentTimeMillis() + TimeParser.parseStringtoMillisecs(ConfigManager.lockdown.LockdownTime);
            setLockedDown(true);
            setExpiryTime(time);
            setOptionalMessage(ConfigManager.lockdown.StartupMsg);
            LoggingManager.log("Startup with Lockdown ON. Expiry in " + ConfigManager.lockdown.LockdownTime + "Msg: "
                    + optionalMessage);
        }
    }


    public static boolean isLockedDown() {
        return lockedDown;
    }

    public static long getExpiryTime() {
        return expiryTime;
    }

    public static String getExpiryTimeString() {
        return Utilities.createTimeStampString(expiryTime);
    }

    public static String getOptionalMessage() {
        return optionalMessage;
    }

    private static void setLockedDown(boolean lockedDown) {
        LockDownManager.lockedDown = lockedDown;
    }

    private static void setExpiryTime(long expiryTime) {
        LockDownManager.expiryTime = expiryTime;
    }

    private static void setOptionalMessage(String optionalMessage) {
        LockDownManager.optionalMessage = optionalMessage;
    }

    public static void startLockDown(Long expiryTime, String msg) {
        startLockDown(null, expiryTime, msg);
    }

    public static void startLockDown(String sender, Long expiryTime, String msg) {
        setExpiryTime(expiryTime);
        setOptionalMessage(msg);
        setLockedDown(true);
        CommandSender target = getSender(sender);
        if (LockDownManager.isLockedDown()) {
            if (target != null) {
                PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Server is locked down until: " + LockDownManager.getExpiryTimeString());
                if (!optionalMessage.isEmpty()) {
                    PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Msg shown to unknown (blocked) players: " + optionalMessage);
                }

                LoggingManager.log("Lockdown start by " + target.getName() + "  ON." +
                        " Expiry in " + Utilities.buildShortTimeDiffString(expiryTime - System.currentTimeMillis(), 2) +
                        " Msg shown to unknown (blocked) players: " + optionalMessage);
            }
        } else {

            if (target != null) {
                PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Lockdown failed to start");
            }
        }

    }

    /**
     * Checks the expiry time of a lockdown and sets the LockDownManager to false if expired.
     * Returns true if lockdown has expired.  False if the lockdown would persist.
     *
     * @return boolean
     */
    public static boolean checkExpiry() {
        return checkExpiry(null);
    }

    public static boolean checkExpiry(String sender) {
        if (isLockedDown()) {
            if (System.currentTimeMillis() >= expiryTime) {
                setExpiryTime(0);
                setLockedDown(false);
                setOptionalMessage(null);
                LoggingManager.log("Lockdown has expired automatically, time and message cleared.");
                CommandSender target = getSender(sender);
                if (target != null) {
                    PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Server is not Locked down");
                }
                return true;
            } else {
                CommandSender target = getSender(sender);
                if (target != null) {
                    PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Server is locked down until: " + LockDownManager.getExpiryTimeString());
                }
                return false;
            }
        } else {

            return true;
        }
    }

    public static void endLockDown() {
        endLockDown(null);
    }

    public static void endLockDown(String sender) {

        setExpiryTime(0);
        setLockedDown(false);
        setOptionalMessage(null);
        CommandSender target = getSender(sender);
        if (!LockDownManager.isLockedDown()) {
            LoggingManager.log("Lockdown has been ended. Time and message cleared.");
            if (target != null) {
                PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Lockdown has been ended");
            }
        } else {
            LoggingManager.log("Lockdown did not end. Critical Error contact Admins");
            if (target != null) {
                PlayerManager.sendMessageToTarget(target, ChatColor.RED + "Lockdown did not end. Critical Error contact Admins");
            }
        }
    }

    private static CommandSender getSender(String sender) {
        if (sender != null) {
            GSPlayer p = PlayerManager.getPlayer(sender);
            return (p == null ? ProxyServer.getInstance().getConsole() : p.getProxiedPlayer());
        }
        return null;
    }

}
