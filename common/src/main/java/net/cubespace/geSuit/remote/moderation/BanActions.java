package net.cubespace.geSuit.remote.moderation;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.storage.StorageException;

public interface BanActions {
    public Result ban(GlobalPlayer player, String reason, String by, UUID byId);
    public Result ban(GlobalPlayer player, String reason, String by, UUID byId, boolean isAuto);
    
    public Result ipban(GlobalPlayer player, String reason, String by, UUID byId);
    public Result ipban(GlobalPlayer player, String reason, String by, UUID byId, boolean isAuto);
    
    public Result ban(InetAddress ip, String reason, String by, UUID byId);
    public Result ban(InetAddress ip, String reason, String by, UUID byId, boolean isAuto);
    
    public Result banUntil(GlobalPlayer player, String reason, long until, String by, UUID byId);
    public Result banUntil(GlobalPlayer player, String reason, long until, String by, UUID byId, boolean isAuto);
    
    public Result ipbanUntil(GlobalPlayer player, String reason, long until, String by, UUID byId);
    public Result ipbanUntil(GlobalPlayer player, String reason, long until, String by, UUID byId, boolean isAuto);
    
    public Result banUntil(InetAddress ip, String reason, long until, String by, UUID byId);
    public Result banUntil(InetAddress ip, String reason, long until, String by, UUID byId, boolean isAuto);
    
    public Result unban(GlobalPlayer player, String reason, String by, UUID byId);
    public Result ipunban(GlobalPlayer player, String reason, String by, UUID byId);
    public Result unban(InetAddress ip, String reason, String by, UUID byId);
    
    public Result kick(GlobalPlayer player, String reason);
    public Result kick(GlobalPlayer player, String reason, boolean isAuto);
    public Result kickAll(String reason);
    
    public List<BanInfo<GlobalPlayer>> getHistory(GlobalPlayer player) throws StorageException;
    public List<BanInfo<InetAddress>> getHistory(InetAddress ip) throws StorageException;
    
    public BanInfo<InetAddress> getIPBan(InetAddress address) throws StorageException;
    public void setIPBan(InetAddress ip, BanInfo<InetAddress> ban) throws StorageException;
    public boolean isIPBanned(InetAddress address) throws StorageException;
}
