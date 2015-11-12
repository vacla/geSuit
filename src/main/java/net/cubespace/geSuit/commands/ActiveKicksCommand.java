package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.ConfigManager;
import net.cubespace.geSuit.objects.Kick;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 8/11/2015.
 */
public class ActiveKicksCommand extends Command {

    public ActiveKicksCommand() {
        super("!ActiveKicks", "gesuit.admin", new String[0]);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        long timeOut = ConfigManager.bans.KicksTimeOut;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeOut);
        sender.sendMessage(TextComponent.fromLegacyText("Kick TimeOut: " + minutes + "m"));
        BansManager.clearKicks();
        List<Kick> kicks = BansManager.getKicks();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        if (kicks.size() == 0) sender.sendMessage(TextComponent.fromLegacyText("No Kicks Active"));
        for (Kick kick : kicks) {
            sender.sendMessage(TextComponent.fromLegacyText("Kicks Active"));
            String dateTime = sdf.format(new Date(kick.getBannedOn() + timeOut));
            sender.sendMessage(TextComponent.fromLegacyText(kick.toString() + " Expiry:" + dateTime));
        }

    }
}
