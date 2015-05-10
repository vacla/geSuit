package net.cubespace.geSuit.teleports.commands;

import net.cubespace.geSuit.core.commands.Command;

import org.bukkit.entity.Player;

public class SpawnCommands {
    @Command(name="setnewspawn", async=true, aliases={"sns", "setnewplayerspawn", "setnoobspawn"}, permission="gesuit.spawns.command.setnewspawn", description="Sets the new players spawn point", usage="/<command>")
    public void setnewspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="setworldspawn", async=true, aliases={"sws"}, permission="gesuit.spawns.command.setworldspawn", description="Sets the world spawn point", usage="/<command>")
    public void setworldspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="setserverspawn", async=true, aliases={"sss"}, permission="gesuit.spawns.command.setserverspawn", description="Sets the servers spawn point", usage="/<command>")
    public void setserverspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="setglobalspawn", async=true, aliases={"sgs", "setproxyspawn", "sethub", "sethubspawn"}, permission="gesuit.spawns.command.setglobalspawn", description="Sets the global spawn point", usage="/<command>")
    public void setglobalspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    
    @Command(name="spawn", async=true, permission="gesuit.spawns.command.spawn", description="Sends the player to the relevent spawn", usage="/<command>")
    public void spawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="worldspawn", async=true, aliases={"ws"}, permission="gesuit.spawns.command.worldspawn", description="Sends the player to the worlds spawn", usage="/<command>")
    public void worldspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="serverspawn", async=true, aliases={"ss"}, permission="gesuit.spawns.command.serverspawn", description="Sends the player to the servers spawn", usage="/<command>")
    public void serverspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="globalspawn", async=true, aliases={"gs", "hub", "lobby"}, permission="gesuit.spawns.command.globalspawn", description="Sends the player to the proxys spawn", usage="/<command>")
    public void globalspawn(Player player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
