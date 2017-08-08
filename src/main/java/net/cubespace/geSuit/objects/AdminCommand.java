package net.cubespace.geSuit.objects;

import java.util.Collections;
import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2017.
 */
public class AdminCommand {
    public String command;
    public String server;
    public String commandSender;
    public List<String> args;

    public AdminCommand(String command, String server, String sender, String... args) {
        this.command = command;
        this.server = server;
        commandSender = sender;
        Collections.addAll(this.args, args);
    }

    public String getServer() {
        return server;
    }

    public String getCommand() {
        return command;
    }

    public String getCommandSender() {
        return commandSender;
    }

    public List<String> getArgs() {
        return args;
    }
}
