package net.cubespace.geSuit.teleports.commands;

import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandPriority;
import net.cubespace.geSuit.core.commands.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommands {
    @Command(name="back", async=true, permission="gesuit.teleports.command.back", description="Sends you back to your last death or teleport location", usage="/<command>")
    public void back(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tphere", async=true, permission="gesuit.teleports.command.tphere", aliases={"teleporthere", "tptome", "tpohere"}, description="Teleports a player to you", usage="/<command> <player>")
    public void tphere(Player player, String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tpahere", async=true, permission="gesuit.teleports.command.tpahere", aliases={"teleportaskhere"}, description="Requests a player teleport to you", usage="/<command> <player>")
    public void tpahere(Player player, String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tpall", async=true, permission="gesuit.teleports.command.tpall", aliases={"teleportall"}, description="Requests all players teleport to you", usage="/<command>")
    public void tpall(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tpa", async=true, permission="gesuit.teleports.command.tpa", aliases={"tpask", "teleportask", "tpto"}, description="Sends a teleport request to a player", usage="/<command> <player>")
    public void tpa(Player player, String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tpaccept", async=true, permission="gesuit.teleports.command.tpaccept", aliases={"teleportaccept", "tpyes"}, description="Accepts a players teleport request", usage="/<command>")
    public void tpaccept(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tpdeny", async=true, permission="gesuit.teleports.command.tpdeny", aliases={"teleportdeny", "tpno"}, description="Denies a players teleport request", usage="/<command>")
    public void tpdeny(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tptoggle", async=true, permission="gesuit.teleports.command.tptoggle", description="Toggles the receiving of tp requests", usage="/<command>")
    public void tptoggle(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="top", async=true, permission="gesuit.teleports.command.top", description="Teleport to the highest block at your current position", usage="/<command>")
    public void top(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <x> <y> <z> [world]")
    public void tp(Player player, int x, int y, int z, @Optional String worldName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <server> <world> <x> <y> <z>")
    @CommandPriority(1)
    public void tp(Player player, String serverName, String worldName, int x, int y, int z) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player>")
    public void tp(Player player, String playerName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player> <target>")
    public void tp(CommandSender sender, String playerName, String targetPlayer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player> <x> <y> <z> [world]")
    @CommandPriority(2)
    public void tp(CommandSender sender, String playerName, int x, int y, int z, @Optional String worldName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player> <server> <world> <x> <y> <z>")
    public void tp(CommandSender sender, String playerName, String serverName, String worldName, int x, int y, int z) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
