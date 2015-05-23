package net.cubespace.geSuit.remote.teleports;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.objects.Result;

public interface TeleportActions {
    public Result tpBack(GlobalPlayer player, boolean allowDeath, boolean allowTeleport);
    
    public Result requestTphere(GlobalPlayer player, GlobalPlayer target, boolean hasBypass);
    
    public Result tpall(GlobalPlayer player);
    
    public Result requestTp(GlobalPlayer player, GlobalPlayer target, boolean hasBypass);
    
    public Result acceptTp(GlobalPlayer player);
    
    public Result rejectTp(GlobalPlayer player);
    
    public Result toggleTp(GlobalPlayer player); 
    
    public Result teleport(GlobalPlayer player, Location target);
    
    public Result teleport(GlobalPlayer player, GlobalPlayer target, boolean isSilent, boolean hasBypass);
}
