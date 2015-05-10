package net.cubespace.geSuit.teleports.commands;

import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;

import org.bukkit.entity.Player;

public class HomeCommands {
    @Command(name="sethome", async=true, permission="gesuit.homes.commands.sethome", description="Sets the players home location", usage="/<command> [name]")
    public void sethome(Player player, @Optional String home) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="delhome", async=true, permission="gesuit.homes.commands.delhome", description="Deletes a players home", usage="/<command> [name]")
    public void delhome(Player player, @Optional String home) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="home", async=true, permission="gesuit.homes.commands.home", description="Teleports you home", usage="/<command> [name]")
    public void home(Player player, @Optional String home) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="homes", async=true, permission="gesuit.homes.commands.homes", description="Lists all of your homes", usage="/<command> [player]")
    public void homes(Player player, @Optional String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
