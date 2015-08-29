package net.cubespace.geSuit.commands;


import net.cubespace.geSuit.TimeParser;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.managers.LockDownManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author benjamincharlton on 26/08/2015.
 */
public class LockdownCommand extends Command {
    public LockdownCommand() {
        super("!lockdown");
    }

    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage(TextComponent.fromLegacyText(Utilities.colorize("&c You cannot peform that command")));
            return;
        }
        if (!(sender.hasPermission("gesuit.admin") || sender.hasPermission("gesuit.lockdown"))) {
            sender.sendMessage(TextComponent.fromLegacyText(ConfigManager.messages.NO_PERMISSION));
            return;
        }
        long lockdowntime = 0;
        if (args.length == 0) {
            long time = TimeParser.parseStringtoMillisecs(ConfigManager.lockdown.LockdownTime);
            if (time > 0) {
                lockdowntime = System.currentTimeMillis() + time;
                LockDownManager.startLockDown(lockdowntime, "");
                sender.sendMessage(TextComponent.fromLegacyText("Lockdown Expiry Time:" + lockdowntime + "System Time:" + System.currentTimeMillis()));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText("Couldnt not parse time: " + ConfigManager.lockdown.LockdownTime));
            }
        }
        if (args.length == 1) {
            if (args[0].equals("end")) {
                LockDownManager.endLockDown();
                if (LockDownManager.isLockedDown()) {
                    sender.sendMessage(TextComponent.fromLegacyText("Lockdown could not end and is persisting"));
                }
                return;
            } else if (args[0].equals("status")) {
                if (LockDownManager.checkExpiry()) {
                    sender.sendMessage(TextComponent.fromLegacyText("Lockdown is not active"));
                } else {
                    sender.sendMessage(TextComponent.fromLegacyText("Lockdown is active until " + LockDownManager.getExpiryTimeString()));
                }
                return;
            }
            try {
                lockdowntime = System.currentTimeMillis() + TimeParser.parseStringtoMillisecs(args[0]);
                if (lockdowntime > System.currentTimeMillis()) {
                    LockDownManager.startLockDown(lockdowntime, "");
                } else {
                    throw new NumberFormatException("Lockdowntime: " + args[0] + " parsed to" + TimeParser.parseStringtoMillisecs(args[0]));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(TextComponent.fromLegacyText("Could not format time from " + args[0]));

                sender.sendMessage(TextComponent.fromLegacyText(ConfigManager.messages.LOCKDOWN_USAGE));

                return;
            }
        }
        if (args.length > 1) {
            try {
                lockdowntime = TimeParser.parseStringtoMillisecs(args[0]);
                if (lockdowntime == 0) {
                    throw new NumberFormatException("Lockdowntime: " + args[0] + " parsed to" + TimeParser.parseStringtoMillisecs(args[0]));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(TextComponent.fromLegacyText("Could not format time from " + args[0]));
                sender.sendMessage(TextComponent.fromLegacyText(ConfigManager.messages.LOCKDOWN_USAGE));
                LoggingManager.log("Could not format time from " + args[0] + "error:" + e.getMessage());
                return;
            }
            long expiryTime = System.currentTimeMillis() + lockdowntime;
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                if (i != 1) {
                    builder.append(' ');
                }

                builder.append(args[i]);
            }

            String message = builder.toString();
            LockDownManager.startLockDown(sender.getName(), expiryTime, message);
        }
    }
}
