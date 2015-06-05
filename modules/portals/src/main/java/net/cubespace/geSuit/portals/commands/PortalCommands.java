package net.cubespace.geSuit.portals.commands;

import net.cubespace.geSuit.core.commands.Command;
import net.cubespace.geSuit.core.commands.Optional;
import net.cubespace.geSuit.core.objects.Portal;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalCommands {
    @Command(name="setportal", async=true, aliases={"createportal", "makeportal", "sportal"}, permission="gesuit.portals.command.setportal", usage="/<command> <name> <type> <destination> [<fill>]")
    public void setportal(Player player, String portalName, Portal.Type type, String destination, @Optional Portal.FillType fill) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="delportal", async=true, aliases={"deleteportal", "dportal", "removeportal"}, permission="gesuit.portals.command.delportal", usage="/<command> <name>")
    public void delportal(Player player, String portalName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @Command(name="portals", async=true, aliases={"portalslist", "portallist", "listportals"}, permission="gesuit.portals.command.portals", usage="/<command>")
    public void portals(CommandSender sender) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
