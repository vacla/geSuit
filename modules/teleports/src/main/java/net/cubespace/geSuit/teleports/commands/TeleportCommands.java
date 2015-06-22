package net.cubespace.geSuit.teleports.commands;

import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.CommandContext;
import net.cubespace.geSuit.core.commands.CommandPriority;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Result.Type;
import net.cubespace.geSuit.remote.teleports.TeleportActions;
import net.cubespace.geSuit.teleports.misc.LocationUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportCommands {
    private TeleportActions actions;
    
    public TeleportCommands(TeleportActions actions) {
        this.actions = actions;
    }
    
    @Command(name="back", async=true, permission="gesuit.teleports.command.back", description="Sends you back to your last death or teleport location", usage="/<command>")
    public void back(Player sender) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        
        Result result = actions.tpBack(player, sender.hasPermission("gesuit.teleports.back.death"), sender.hasPermission("gesuit.teleports.back.teleport"));
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tphere", async=true, permission="gesuit.teleports.command.tphere", aliases={"teleporthere", "tptome", "tpohere"}, description="Teleports a player to you", usage="/<command> <player>")
    public void tphere(Player sender, String playerName) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        GlobalPlayer target = Global.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        Result result = actions.teleport(target, player, sender.hasPermission("gesuit.teleports.tp.silent"), sender.hasPermission("gesuit.teleports.tp.bypass"));
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tpahere", async=true, permission="gesuit.teleports.command.tpahere", aliases={"teleportaskhere"}, description="Requests a player teleport to you", usage="/<command> <player>")
    public void tpahere(Player sender, String playerName) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        GlobalPlayer target = Global.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        Result result = actions.requestTphere(player, target, sender.hasPermission("gesuit.teleports.tp.bypass"));
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tpall", async=true, permission="gesuit.teleports.command.tpall", aliases={"teleportall"}, description="Requests all players teleport to you", usage="/<command>")
    public void tpall(Player sender) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        
        Result result = actions.tpall(player);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tpa", async=true, permission="gesuit.teleports.command.tpa", aliases={"tpask", "teleportask", "tpto"}, description="Sends a teleport request to a player", usage="/<command> <player>")
    public void tpa(Player sender, String playerName) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        GlobalPlayer target = Global.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        Result result = actions.requestTp(player, target, sender.hasPermission("gesuit.teleports.tp.bypass"));
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tpaccept", async=true, permission="gesuit.teleports.command.tpaccept", aliases={"teleportaccept", "tpyes"}, description="Accepts a players teleport request", usage="/<command>")
    public void tpaccept(Player sender) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        
        Result result = actions.acceptTp(player);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tpdeny", async=true, permission="gesuit.teleports.command.tpdeny", aliases={"teleportdeny", "tpno"}, description="Denies a players teleport request", usage="/<command>")
    public void tpdeny(Player sender) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        
        Result result = actions.rejectTp(player);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tptoggle", async=true, permission="gesuit.teleports.command.tptoggle", description="Toggles the receiving of tp requests", usage="/<command>")
    public void tptoggle(Player sender) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        
        Result result = actions.toggleTp(player);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="top", async=false, permission="gesuit.teleports.command.top", description="Teleport to the highest block at your current position", usage="/<command>")
    public void top(Player sender) {
        org.bukkit.Location current = sender.getLocation();
        org.bukkit.Location location = LocationUtil.getSafeDestination(
            new org.bukkit.Location(
                current.getWorld(), 
                current.getX(), 
                current.getWorld().getMaxHeight(), 
                current.getZ(), 
                current.getYaw(), 
                current.getPitch()
            )
        );
        
        sender.teleport(location, TeleportCause.COMMAND);
        sender.sendMessage(Global.getMessages().get("top.success"));
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <x> <y> <z> [world]")
    public void tp(CommandContext<Player> context, int x, int y, int z, @Optional String worldName) {
    	if (context.isErrored()) {
    		switch (context.getErrorArg()) {
    		case 0:
    		case 1:
    		case 2:
    			context.setErrorMessage(Global.getMessages().get("teleport.error.location", "value", context.getErrorInput()));
    			break;
    		}
    		
    		return;
    	}
    	
    	Player sender = context.getSender();
        GlobalPlayer player = Global.getPlayer(context.getSender().getUniqueId());
        
        if (worldName != null) {
            if (Bukkit.getWorld(worldName) == null) {
                context.sendMessage(Global.getMessages().get("teleport.error.world", "world", worldName));
                return;
            }
        } else {
            worldName = sender.getWorld().getName();
        }
        
        // See if teleporting is allowed here
        if (!sender.teleport(sender.getLocation(), TeleportCause.COMMAND)) {
        	sender.sendMessage(Global.getMessages().get("teleport.blocked.area"));
            return;
        }
        
        Location target = new Location(null, worldName, x, y, z, sender.getLocation().getYaw(), sender.getLocation().getPitch());
        
        Result result = actions.teleport(player, target);
        
        if (result.getType() == Type.Fail && result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <server> <world> <x> <y> <z>")
    @CommandPriority(1)
    public void tp(CommandContext<Player> context, String serverName, String worldName, int x, int y, int z) {
    	if (context.isErrored()) {
    		switch (context.getErrorArg()) {
    		case 2:
    		case 3:
    		case 4:
    			context.setErrorMessage(Global.getMessages().get("teleport.error.location", "value", context.getErrorInput()));
    			break;
    		}
    		
    		return;
    	}
    	
    	Player sender = context.getSender();
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        
        // See if teleporting is allowed here
        if (!sender.teleport(sender.getLocation(), TeleportCause.COMMAND)) {
            sender.sendMessage(Global.getMessages().get("teleport.blocked.area"));
            return;
        }
        
        Location target = new Location(serverName, worldName, x, y, z, sender.getLocation().getYaw(), sender.getLocation().getPitch());
        
        Result result = actions.teleport(player, target);
        
        if (result.getType() == Type.Fail && result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }

    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player>")
    public void tp(Player sender, String playerName) {
        GlobalPlayer player = Global.getPlayer(sender.getUniqueId());
        GlobalPlayer target = Global.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        // See if teleporting is allowed here
        if (!sender.teleport(sender.getLocation(), TeleportCause.COMMAND)) {
            sender.sendMessage(Global.getMessages().get("teleport.blocked.area"));
            return;
        }
        
        Result result = actions.teleport(player, target, sender.hasPermission("gesuit.teleports.tp.silent"), sender.hasPermission("gesuit.teleports.tp.bypass"));
        
        if (result.getType() == Type.Fail && result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player> <target>")
    public void tp(CommandSender sender, String playerName, String targetPlayer) {
        GlobalPlayer player = Global.getPlayer(playerName);
        GlobalPlayer target = Global.getPlayer(targetPlayer);
        
        if (player == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        if (target == null) {
            sender.sendMessage(Global.getMessages().get("player.unknown", "player", targetPlayer));
            return;
        }
        
        Result result = actions.teleport(player, target, false, true);
        
        if (result.getMessage() != null) {
            sender.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player> <x> <y> <z> [world]")
    @CommandPriority(2)
    public void tp(CommandContext<CommandSender> context, String playerName, int x, int y, int z, @Optional String worldName) {
    	if (context.isErrored()) {
    		switch (context.getErrorArg()) {
    		case 1:
    		case 2:
    		case 3:
    			context.setErrorMessage(Global.getMessages().get("teleport.error.location", "value", context.getErrorInput()));
    			break;
    		}
    		
    		return;
    	}
    	
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            context.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        if (worldName != null) {
            if (Bukkit.getWorld(worldName) == null) {
                context.sendMessage(Global.getMessages().get("teleport.error.world", "world", worldName));
                return;
            }
        }
        
        Location target = new Location(null, worldName, x, y, z);
        
        Result result = actions.teleport(player, target);
        
        if (result.getMessage() != null) {
            context.sendMessage(result.getMessage());
        }
    }
    
    @Command(name="tp", async=true, permission="gesuit.teleports.command.tp", aliases={"teleport", "tpo"}, description="Teleports a player to another player or location", usage="/<command> <player> <server> <world> <x> <y> <z>")
    public void tp(CommandContext<CommandSender> context, String playerName, String serverName, String worldName, int x, int y, int z) {
    	if (context.isErrored()) {
    		switch (context.getErrorArg()) {
    		case 3:
    		case 4:
    		case 5:
    			context.setErrorMessage(Global.getMessages().get("teleport.error.location", "value", context.getErrorInput()));
    			break;
    		}
    		
    		return;
    	}
    	
        GlobalPlayer player = Global.getPlayer(playerName);
        
        if (player == null) {
            context.sendMessage(Global.getMessages().get("player.unknown", "player", playerName));
            return;
        }
        
        Location target = new Location(serverName, worldName, x, y, z);
        
        Result result = actions.teleport(player, target);
        
        if (result.getMessage() != null) {
            context.sendMessage(result.getMessage());
        }
    }
}
