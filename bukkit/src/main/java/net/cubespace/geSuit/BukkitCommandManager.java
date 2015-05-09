package net.cubespace.geSuit;

import java.lang.reflect.Method;
import java.util.Collection;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.commands.WrapperCommand;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;

public class BukkitCommandManager extends CommandManager {
    private SimpleCommandMap commandMap;
    
    public BukkitCommandManager() {
        try
        {
            Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
            commandMap = (SimpleCommandMap)method.invoke(Bukkit.getServer());
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void installCommands(Collection<WrapperCommand> commands) {
        for (WrapperCommand command : commands) {
            try {
                command.bake();
                commandMap.register("gesuit", command);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }
}
