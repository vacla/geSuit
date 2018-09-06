package net.cubespace.geSuit.tasks;

import net.cubespace.geSuit.Utilities;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;

public class ServerAnnouncements implements Runnable
{

    ArrayList<String> list = new ArrayList<>();
    int count = 0;
    ServerInfo server;

    public ServerAnnouncements(ServerInfo server)
    {
        this.server = server;
    }

    public void addAnnouncement(String message)
    {
        list.add(Utilities.colorize(message));
    }

    public void run()
    {
        if (list.isEmpty()) {
            return;
        }
        if (server.getPlayers().isEmpty()) {
            return;
        }
        for (ProxiedPlayer player : server.getPlayers()) {
            for (String line : list.get(count).split("\n")) {
                // not sure if everything is thread safe. In doubt, leaving that one. It's colorized anyway.
                player.sendMessage(TextComponent.fromLegacyText(Utilities.colorize(line)));
            }
        }
        count++;
        if ((count + 1) > list.size()) {
            count = 0;
        }
    }
}
