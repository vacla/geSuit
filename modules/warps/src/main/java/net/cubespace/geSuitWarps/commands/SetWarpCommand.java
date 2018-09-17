package net.cubespace.geSuitWarps.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitWarps.managers.WarpsManager;


public class SetWarpCommand extends CommandManager<WarpsManager> {

    public SetWarpCommand(WarpsManager manager) {
        super(manager);
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
				if(args.length==1){
					// Create the named global warp
                    manager.setWarp(sender, args[0], false, true);
					return true;
				}else if (args.length==2){
					// Create the named global warp, hiding it if the second argument is true
					try{
                        manager.setWarp(sender, args[0], Boolean.parseBoolean(args[1]), true);
					}catch(Exception e){
						sender.sendMessage(ChatColor.RED+"invalid argument supplied");
						return false;
					}
					return true;
				}else if (args.length==3){
					// Create the named warp, optionally hiding it, and global if the 3rd argument is true, otherwise server-specific
					try{
                        manager.setWarp(sender, args[0], Boolean.parseBoolean(args[1]), Boolean.parseBoolean(args[2]));
				}catch(Exception e){
					sender.sendMessage(ChatColor.RED+"invalid argument supplied");
					return false;
				}
					return true;
				}
				return false;

	}

}

