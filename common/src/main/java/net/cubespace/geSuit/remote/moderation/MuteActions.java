package net.cubespace.geSuit.remote.moderation;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Tuple;

public interface MuteActions {
    public static final long Permanent = -1;
    
    public Result enableGlobalMute(String byName);
    public Result enableGlobalMute(long until, String byName);
    public Result disableGlobalMute();
    public Tuple<Boolean, Long> getGlobalMute();
    
    public Result mute(GlobalPlayer who, String byName);
    public Result mute(GlobalPlayer who, long until, String byName);
    public Result unmute(GlobalPlayer who);
    public boolean isMuted(GlobalPlayer who);
    
    public Map<UUID, Long> getMutedPlayers();
    
    public Result mute(InetAddress who, String byName);
    public Result mute(InetAddress who, long until, String byName);
    public Result unmute(InetAddress who);
    public boolean isMuted(InetAddress who);
    
    public Result muteIp(GlobalPlayer who, String byName);
    public Result muteIp(GlobalPlayer who, long until, String byName);
    public Result unmuteIp(GlobalPlayer who);
    
    public Map<Tuple<InetAddress, String>, Long> getMutedIPs();
}
