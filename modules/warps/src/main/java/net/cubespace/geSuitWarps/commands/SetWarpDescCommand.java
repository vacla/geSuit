package net.cubespace.geSuitWarps.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.cubespace.geSuit.managers.CommandManager;
import net.cubespace.geSuitWarps.managers.WarpsManager;


public class SetWarpDescCommand extends CommandManager<WarpsManager> {

    public SetWarpDescCommand(WarpsManager manager) {
        super(manager);
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command,
							 String label, String[] args) {

		if (args.length > 1) {

			// Update the description of the named warp
			String warpName = args[0];

			// Combine arguments 1 and higher
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i < args.length; i++)
				sb.append(args[i] + " ");

			String warpDescription = sb.toString().trim();

			// A description of simply a period means to clear the warp description
			if (warpDescription.equalsIgnoreCase("."))
				warpDescription = "";

			try {
                manager.setWarpDesc(sender, warpName, warpDescription);
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + "Error setting the warp description");
				return false;
			}
			return true;
		}
		return false;

	}

}

