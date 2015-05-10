package net.cubespace.geSuit.teleports.commands;

import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommands {
    @Command(name="warps", async=true, aliases={"warplist"}, permission="gesuit.warps.command.warps", description="Shows a list of available warps", usage="/<command>")
    public void warps(CommandSender sender) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="warp", async=true, aliases={"warpto"}, permission="gesuit.warps.command.warp", description="Warps a player to a specific warp", usage="/<command> [player] <warp>")
    public void warp(CommandSender sender, @Optional String playerName, String warp) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name>")
    public void setwarp(Player sender, String warp) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name> <hidden true/false>")
    public void setwarp(Player sender, String warp, boolean hidden) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="setwarp", async=true, aliases={"createwarp"}, permission="gesuit.warps.command.setwarp", description="Sets a warps at the players location", usage="/<command> <name> <hidden true/false> <global true/false>")
    public void setwarp(Player sender, String warp, boolean hidden, boolean global) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="delwarp", async=true, aliases={"deletewarp", "removewarp"}, permission="gesuit.warps.command.delwarp", description="Used to delete a specific warp", usage="/<command> <name>")
    public void delwarp(CommandSender sender, String name) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
