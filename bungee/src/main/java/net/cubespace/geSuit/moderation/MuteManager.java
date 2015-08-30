package net.cubespace.geSuit.moderation;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.config.ModerationConfig.MuteSettings;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.PlayerManager;
import net.cubespace.geSuit.core.lang.Messages;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Result.Type;
import net.cubespace.geSuit.core.objects.Tuple;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.remote.moderation.MuteActions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class MuteManager implements MuteActions, ConfigReloadListener {
    private static final String globalExceptPermission = "gesuit.mute.global.exempt";
    private final BroadcastManager broadcasts;
    private final Messages messages;
    private final ProxyServer proxy;
    private final PlayerManager playerManager;
    
    private Map<InetAddress, Tuple<Long, String>> mutedIps; // Value is expiry time and display name
    private Map<UUID, Long> mutedPlayers;
    
    private boolean globalMuteActive;
    private long globalMuteEnd;
    
    // Configuration
    private boolean broadcastMute;
    private boolean broadcastUnmute;
    private boolean broadcastAutoUnmute;
    private boolean broadcastGlobal;
    
    private long maxMuteLength;
    private boolean allowPermanentMutes;
    private boolean allowReMute;
    
    private Set<String> commandList;
    private boolean isCommandWhitelist;
    
    public MuteManager(BroadcastManager broadcasts, Messages messages, ProxyServer proxy, PlayerManager playerManager) {
        this.broadcasts = broadcasts;
        this.messages = messages;
        this.proxy = proxy;
        this.playerManager = playerManager;
        
        mutedIps = Collections.synchronizedMap(Maps.<InetAddress,Tuple<Long, String>>newHashMap());
        mutedPlayers = Collections.synchronizedMap(Maps.<UUID,Long>newHashMap());
        
        commandList = Sets.newHashSet();
    }
    
    public void loadConfig(ModerationConfig config) {
        MuteSettings settings = config.Mutes;
        broadcastMute = settings.BroadcastMute;
        broadcastUnmute = settings.BroadcastUnmute;
        broadcastAutoUnmute = settings.BroadcastAutoUnmute;
        broadcastGlobal = settings.BroadcastGlobal;
        
        allowPermanentMutes = settings.AllowPermanentMutes;
        allowReMute = settings.AllowReMute;
        
        try {
            if (Strings.isNullOrEmpty(settings.MaximumMuteDuration) || settings.MaximumMuteDuration.equalsIgnoreCase("none")) {
                maxMuteLength = Long.MAX_VALUE;
            } else {
                maxMuteLength = DateDiff.valueOf(settings.MaximumMuteDuration).toMillis();
            }
        } catch (IllegalArgumentException e) {
            maxMuteLength = TimeUnit.MINUTES.toMillis(20);
        }
        
        commandList.clear();
        for (String command : settings.CommandsList) {
            commandList.add(command.toLowerCase());
        }
        
        isCommandWhitelist = settings.CommandListIsWhitelist;
    }
    
    @Override
    public void onConfigReloaded(ConfigManager manager) {
        loadConfig(manager.moderation());
    }
    
    @Override
    public Result enableGlobalMute(String byName) {
        return enableGlobalMute(Permanent, byName);
    }

    @Override
    public Result enableGlobalMute(long until, String byName) {
        if (!allowReMute && globalMuteActive) {
            return new Result(Type.Fail, messages.get("mute.already-active.global"));
        }
        
        String message;
        
        if (until == Permanent) {
            message = messages.get("mute.start.global", "by", byName);
            globalMuteEnd = Permanent;
        } else {
            String timeString = new DateDiff(until - System.currentTimeMillis()).toString(2);
            message = messages.get("mute.start.global.time", "by", byName, "time", timeString);
            globalMuteEnd = until;
        }
        
        globalMuteActive = true;
        
        if (broadcastGlobal) {
            broadcasts.broadcastGlobal(message);
            return new Result(Type.Success, null);
        } else {
            return new Result(Type.Success, message);
        }
    }

    @Override
    public Result disableGlobalMute() {
        if (!globalMuteActive) {
            return new Result(Type.Fail, messages.get("mute.not-active.global"));
        }
        
        globalMuteActive = false;
        String message = messages.get("mute.end.global");
        if (broadcastGlobal) {
            broadcasts.broadcastGlobal(message);
            return new Result(Type.Success, null);
        } else {
            return new Result(Type.Success, message);
        }
    }
    
    @Override
    public Tuple<Boolean, Long> getGlobalMute() {
        if (globalMuteActive) {
            return new Tuple<Boolean, Long>(true, globalMuteEnd);
        } else {
            return new Tuple<Boolean, Long>(false, 0L);
        }
    }

    @Override
    public Result mute(GlobalPlayer who, String byName) {
        return mute(who, Permanent, byName);
    }

    @Override
    public Result mute(GlobalPlayer who, long until, String byName) {
        if (!allowReMute && isMuted(who)) {
            return new Result(Type.Fail, messages.get("mute.already-active.single", "player", who.getDisplayName()));
        }
        
        if (!allowPermanentMutes && until == Permanent) {
            return new Result(Type.Fail, messages.get("mute.no-permanent"));
        }
        
        if (until != Permanent && until - System.currentTimeMillis() > maxMuteLength) {
            return new Result(Type.Fail, messages.get("mute.too-long", "time", new DateDiff(maxMuteLength).toLongString(2)));
        }
        
        mutedPlayers.put(who.getUniqueId(), until);
        String broadcast;
        String pm;
        
        if (until == Permanent) {
            broadcast = messages.get("mute.start.single.broadcast", "player", who.getDisplayName(), "by", byName);
            pm = messages.get("mute.start.single", "player", who.getDisplayName(), "by", byName);
        } else {
            String timeString = new DateDiff(until - System.currentTimeMillis()).toString(2);
            broadcast = messages.get("mute.start.single.time.broadcast", "player", who.getDisplayName(), "by", byName, "time", timeString);
            pm = messages.get("mute.start.single.time", "player", who.getDisplayName(), "by", byName, "time", timeString);
        }
        
        broadcasts.sendMessage(who, pm);
        if (broadcastMute) {
            broadcasts.broadcastGlobal(broadcast);
            return new Result(Type.Success, null);
        } else {
            return new Result(Type.Success, broadcast);
        }
    }

    @Override
    public Result unmute(GlobalPlayer who) {
        if (!isMuted(who)) {
            return new Result(Type.Fail, messages.get("mute.not-active.single", "player", who.getDisplayName()));
        }
        
        mutedPlayers.remove(who.getUniqueId());
        
        String broadcast = messages.get("mute.end.single.broadcast", "player", who.getDisplayName());
        String pm = messages.get("mute.end.single");
        
        broadcasts.sendMessage(who, pm);
        if (broadcastUnmute) {
            broadcasts.broadcastGlobal(broadcast);
            return new Result(Type.Success, null);
        } else {
            return new Result(Type.Success, broadcast);
        }
    }
    
    @Override
    public boolean isMuted(GlobalPlayer who) {
        Long time = mutedPlayers.get(who.getUniqueId());
        if (time != null) {
            return time == Permanent || System.currentTimeMillis() < time;
        } else {
            return false;
        }
    }
    
    @Override
    public Map<UUID, Long> getMutedPlayers() {
        Map<UUID, Long> muted = Maps.newLinkedHashMap();
        
        synchronized(mutedPlayers) {
            for (Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
                if (!isExpired(entry.getValue())) {
                    muted.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        return muted;
    }

    @Override
    public Result mute(InetAddress who, String byName) {
        return mute(who, Permanent, byName);
    }

    @Override
    public Result mute(InetAddress who, long until, String byName) {
        // Find a name to display
        String displayName = who.getHostAddress();
        for (ProxiedPlayer player : proxy.getPlayers()) {
            if (player.getAddress().getAddress().equals(who)) {
                displayName = player.getDisplayName();
                break;
            }
        }
        
        return mute(who, displayName, until, byName);
    }
    
    private Result mute(InetAddress who, String displayName, long until, String byName) {
        if (!allowReMute && isMuted(who)) {
            return new Result(Type.Fail, messages.get("mute.already-active.group", "player", displayName));
        }
        
        if (!allowPermanentMutes && until == Permanent) {
            return new Result(Type.Fail, messages.get("mute.no-permanent"));
        }
        
        if (until != Permanent && until - System.currentTimeMillis() > maxMuteLength) {
            return new Result(Type.Fail, messages.get("mute.too-long", "time", new DateDiff(maxMuteLength).toLongString(2)));
        }
        
        mutedIps.put(who, new Tuple<Long,String>(until, displayName));
        String broadcast;
        String pm;
        
        if (until == Permanent) {
            broadcast = messages.get("mute.start.group.broadcast", "player", displayName, "by", byName);
            pm = messages.get("mute.start.group", "player", displayName, "by", byName);
        } else {
            String timeString = new DateDiff(until - System.currentTimeMillis()).toString(2);
            broadcast = messages.get("mute.start.group.time.broadcast", "player", displayName, "by", byName, "time", timeString);
            pm = messages.get("mute.start.group.time", "player", displayName, "by", byName, "time", timeString);
        }
        
        // Show a message to each player who is affected
        for (ProxiedPlayer player : proxy.getPlayers()) {
            if (player.getAddress().getAddress().equals(who)) {
                player.sendMessage(TextComponent.fromLegacyText(pm));
            }
        }
        
        if (broadcastMute) {
            broadcasts.broadcastGlobal(broadcast);
            return new Result(Type.Success, null);
        } else {
            return new Result(Type.Success, broadcast);
        }
    }

    @Override
    public Result unmute(InetAddress who) {
        // Find a name to display
        String displayName = who.getHostAddress();
        for (ProxiedPlayer player : proxy.getPlayers()) {
            if (player.getAddress().getAddress().equals(who)) {
                displayName = player.getDisplayName();
                break;
            }
        }
        
        return unmute(who, displayName);
    }
    
    private Result unmute(InetAddress who, String displayName) {
        if (!isMuted(who)) {
            return new Result(Type.Fail, messages.get("mute.not-active.group", "player", displayName));
        }
        
        mutedIps.remove(who);
        
        String broadcast = messages.get("mute.end.group.broadcast", "player", displayName);
        String pm = messages.get("mute.end.group");
        
        // Show a message to each player who is affected
        for (ProxiedPlayer player : proxy.getPlayers()) {
            if (player.getAddress().getAddress().equals(who)) {
                player.sendMessage(TextComponent.fromLegacyText(pm));
            }
        }
        
        if (broadcastUnmute) {
            broadcasts.broadcastGlobal(broadcast);
            return new Result(Type.Success, null);
        } else {
            return new Result(Type.Success, broadcast);
        }
    }
    
    @Override
    public boolean isMuted(InetAddress who) {
        Tuple<Long,String> time = mutedIps.get(who);
        if (time != null) {
            return time.getA() == Permanent || System.currentTimeMillis() < time.getA();
        } else {
            return false;
        }
    }
    
    @Override
    public Result muteIp(GlobalPlayer who, String byName) {
        return muteIp(who, Permanent, byName);
    }
    
    @Override
    public Result muteIp(GlobalPlayer who, long until, String byName) {
        return mute(who.getAddress(), who.getDisplayName(), until, byName);
    }
    
    @Override
    public Result unmuteIp(GlobalPlayer who) {
        return unmute(who.getAddress(), who.getDisplayName());
    }
    
    @Override
    public Map<Tuple<InetAddress, String>, Long> getMutedIPs() {
        Map<Tuple<InetAddress, String>, Long> muted = Maps.newLinkedHashMap();
        
        synchronized(mutedIps) {
            for (Entry<InetAddress, Tuple<Long, String>> entry : mutedIps.entrySet()) {
                if (!isExpired(entry.getValue().getA())) {
                    muted.put(new Tuple<InetAddress, String>(entry.getKey(), entry.getValue().getB()), entry.getValue().getA());
                }
            }
        }
        
        return muted;
    }
    
    public void startMuteCheckTimer(Plugin plugin) {
        proxy.getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                expireAnyMutes();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    public void expireAnyMutes() {
        expirePlayerMutes();
        expireIPMutes();
        expireGlobalMute();
    }
    
    private void expirePlayerMutes() {
        synchronized(mutedPlayers) {
            Iterator<Entry<UUID, Long>> it = mutedPlayers.entrySet().iterator();
            
            while (it.hasNext()) {
                Entry<UUID, Long> entry = it.next();
                
                if (isExpired(entry.getValue())) {
                    it.remove();
                    
                    // If the player is still online, send them a message about being unmuted
                    GlobalPlayer player = playerManager.getPlayer(entry.getKey());
                    if (player != null) {
                        broadcasts.sendMessage(player, messages.get("mute.end.single"));
                    }
                    
                    // Do the broadcast if wanted
                    if (broadcastAutoUnmute) {
                        if (player == null) {
                            player = playerManager.getOfflinePlayer(entry.getKey());
                        }
                        
                        broadcasts.broadcastGlobal(messages.get("mute.end.single.broadcast", "player", player.getDisplayName()));
                    }
                }
            }
        }
    }
    
    private void expireIPMutes() {
        synchronized(mutedIps) {
            Iterator<Entry<InetAddress, Tuple<Long, String>>> it = mutedIps.entrySet().iterator();
            
            while (it.hasNext()) {
                Entry<InetAddress, Tuple<Long, String>> entry = it.next();
                
                if (isExpired(entry.getValue().getA())) {
                    it.remove();
                    
                    // Notify muted players
                    String message = messages.get("mute.end.group");
                    for (ProxiedPlayer player : proxy.getPlayers()) {
                        if (player.getAddress().getAddress().equals(entry.getKey())) {
                            player.sendMessage(TextComponent.fromLegacyText(message));
                        }
                    }
                    
                    // Do the broadcast if wanted
                    if (broadcastAutoUnmute) {
                        broadcasts.broadcastGlobal(messages.get("mute.end.group.broadcast", "player", entry.getValue().getB()));
                    }
                }
            }
        }
    }
    
    private void expireGlobalMute() {
        if (globalMuteActive && isExpired(globalMuteEnd)) {
            globalMuteActive = false;
            
            if (broadcastGlobal) {
                broadcasts.broadcastGlobal(messages.get("mute.end.global"));
            }
        }
    }
    
    private boolean isExpired(long expiry) {
        return expiry != Permanent && System.currentTimeMillis() >= expiry;
    }
    
    public boolean isCommandAllowed(String command) {
        if (isCommandWhitelist) {
            return commandList.contains(command);
        } else {
            return !commandList.contains(command);
        }
    }
    
    public boolean checkAllowChat(ProxiedPlayer player, boolean command, String message) {
        if (command) {
            return checkAllowCommand(player, message.split(" ")[0].substring(1));
        } else {
            return checkAllowChat(player);
        }
    }
    
    private boolean checkAllowCommand(ProxiedPlayer player, String command) {
        if (isCommandAllowed(command.toLowerCase())) {
            return true;
        }
        
        if (globalMuteActive) {
            if (player.hasPermission(globalExceptPermission)) {
                return true;
            }
            player.sendMessage(messages.get("mute.command.global"));
            return false;
        }
        
        if (isMuted(playerManager.getPlayer(player.getUniqueId()))) {
            player.sendMessage(messages.get("mute.command.single"));
            return false;
        }
        
        if (isMuted(player.getAddress().getAddress())) {
            player.sendMessage(messages.get("mute.command.group"));
            return false;
        }
        
        return true;
    }
    
    private boolean checkAllowChat(ProxiedPlayer player) {
        if (globalMuteActive) {
            if (player.hasPermission(globalExceptPermission)) {
                return true;
            }
            
            player.sendMessage(messages.get("mute.talk.global"));
            return false;
        }
        
        if (isMuted(playerManager.getPlayer(player.getUniqueId()))) {
            player.sendMessage(messages.get("mute.talk.single"));
            return false;
        }
        
        if (isMuted(player.getAddress().getAddress())) {
            player.sendMessage(messages.get("mute.talk.group"));
            return false;
        }
        
        return true;
    }
}
