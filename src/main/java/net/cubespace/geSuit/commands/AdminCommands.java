package net.cubespace.geSuit.commands;

import net.cubespace.geSuit.managers.AdminCommandManager;
import net.cubespace.geSuit.managers.PlayerManager;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class AdminCommands extends Command {
    /**
     * Construct a new command with no permissions or aliases.
     */
    public AdminCommands() {
        super("!adminCommand" );
    }

    /**
     * Execute this command with the specified sender and arguments.
     *
     * @param sender the executor of this command
     * @param args   arguments used to invoke this command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            return;
        }
        switch(args[0]){
        case "restart":
            if(args.length != 3){
               displayHelp();
               break;
            }
            String server = args[1];
            String timeString = args[2];
            AdminCommandManager.sendAdminCommand(sender,server,"restart", timeString);
            PlayerManager.sendMessageToTarget(sender,"Server Restart Requested for " + server+ " in " + timeString);
            break;
        default:
            displayHelp();
            break;
        }

    }
    public void displayHelp(){
        //todo
    }
}
