package net.cubespace.geSuit.teleports;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.cubespace.geSuit.geSuitPlugin;
import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.TeleportsConfig;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.TeleportMessage;
import net.cubespace.geSuit.core.messages.TeleportRequestMessage;
import net.cubespace.geSuit.core.messages.UpdateBackMessage;
import net.cubespace.geSuit.core.objects.Location;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.Result.Type;
import net.cubespace.geSuit.remote.teleports.TeleportActions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class TeleportsManager implements TeleportActions, ChannelDataReceiver<BaseMessage>, ConfigReloadListener {
    private Channel<BaseMessage> channel;
    private Multimap<ServerInfo, ServerInfo> tpaWhitelist;
    private int tpaExpireTime;
    
    private geSuitPlugin plugin;
    private ProxyServer proxy;
    
    public TeleportsManager(Channel<BaseMessage> channel, geSuitPlugin plugin) {
        this.plugin = plugin;
        this.channel = channel;
        
        proxy = plugin.getProxy();
        channel.addReceiver(this);
    }
    
    private int getServerId(ServerInfo server) {
        // TODO: Make this a method in a proxy global location to resolve id for future updates
        return server.getAddress().getPort();
    }
    
    private ServerInfo getServer(int id) {
        if (id == 0) {
            return null;
        }
        
        for (ServerInfo server : proxy.getServers().values()) {
            if (server.getAddress().getPort() == id) {
                return server;
            }
        }
        
        return null;
    }
    
    public void loadConfig(TeleportsConfig config) {
        tpaWhitelist = HashMultimap.create();
        
        for (String definition : config.TPAWhitelist) {
            String[] parts = definition.split(":");
            
            Preconditions.checkArgument(parts.length == 2, "Error in TPA whitelist definition '" + definition + "'. Should be in the format '<server>:<server>'. <server> can be replaced with 'ALL' as a wildcard");
            
            ServerInfo source;
            ServerInfo dest;
            if (parts[0].equals("ALL")) {
                source = null;
            } else {
                source = proxy.getServerInfo(parts[0]);
                Preconditions.checkNotNull(source, "Error in TPA whitelist definition '" + definition + "'. Unknown server '" + parts[0] + "'");
            }
            
            if (parts[1].equals("ALL")) {
                dest = null;
            } else {
                dest = proxy.getServerInfo(parts[1]);
                Preconditions.checkNotNull(dest, "Error in TPA whitelist definition '" + definition + "'. Unknown server '" + parts[1] + "'");
            }
            
            tpaWhitelist.put(source, dest);
        }
        
        tpaExpireTime = config.TeleportRequestExpireTime;
    }
    
    @Override
    public void onConfigReloaded(ConfigManager manager) {
        loadConfig(manager.teleports());
    }
    
    public boolean isTPAWhitelisted(ServerInfo source, ServerInfo target) {
        return tpaWhitelist.containsEntry(source, target) ||
                tpaWhitelist.containsEntry(null, target) ||
                tpaWhitelist.containsEntry(source, null) ||
                tpaWhitelist.containsEntry(null, null);
    }
    
    public boolean isTPAWhitelisted(Server source, Server target) {
        return isTPAWhitelisted(source.getInfo(), target.getInfo());
    }
    
    private TeleportsAttachment getAttachment(GlobalPlayer player) {
        TeleportsAttachment attachment = player.getAttachment(TeleportsAttachment.class);
        if (attachment == null) {
            attachment = new TeleportsAttachment();
            player.addAttachment(attachment);
        }
        
        return attachment;
    }
    
    private boolean isOnline(GlobalPlayer player) {
        return ProxyServer.getInstance().getPlayer(player.getUniqueId()) != null;
    }
    
    private void sendMessage(GlobalPlayer player, String message) {
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        if (pPlayer != null) {
            pPlayer.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
        }
    }
    
    @Override
    public Result tpBack(GlobalPlayer player, boolean allowDeath, boolean allowTeleport) {
        TeleportsAttachment attachment = getAttachment(player);
        
        if (allowDeath && allowTeleport) {
            if (attachment.hasLastLocation()) {
                teleport0(player, attachment.getLastLocation());
                sendMessage(player, Global.getMessages().get("back.teleport"));
                return new Result(Type.Success, null);
            } else {
                return new Result(Type.Fail, Global.getMessages().get("back.not-set"));
            }
        } else if (allowDeath) {
            if (attachment.hasLastDeath()) {
                teleport0(player, attachment.getLastDeath());
                sendMessage(player, Global.getMessages().get("back.teleport"));
                return new Result(Type.Success, null);
            } else {
                return new Result(Type.Fail, Global.getMessages().get("back.not-set"));
            }
        } else if (allowTeleport) {
            if (attachment.hasLastTeleport()) {
                teleport0(player, attachment.getLastTeleport());
                sendMessage(player, Global.getMessages().get("back.teleport"));
                return new Result(Type.Success, null);
            } else {
                return new Result(Type.Fail, Global.getMessages().get("back.not-set"));
            }
        } else {
            return new Result(Type.Fail, Global.getMessages().get("back.not-set"));
        }
    }
    
    @Override
    public Result requestTphere(final GlobalPlayer player, final GlobalPlayer target, boolean hasBypass) {
        final TeleportsAttachment playerTp = getAttachment(player);
        final TeleportsAttachment targetTp = getAttachment(target);
        
        if (playerTp.getTPA() != null && playerTp.getTPAHere() != null) {
            return new Result(Type.Fail, Global.getMessages().get("tpa.request.blocked"));
        }
        
        if (!isOnline(target)) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", target.getDisplayName()));
        }
        
        if (player.equals(target)) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.player", "player", player.getDisplayName()));
        }
        
        if (!hasBypass && !target.hasTPsEnabled()) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.player", "player", target.getDisplayName()));
        }
        
        if (targetTp.getTPA() != null && targetTp.getTPAHere() != null) {
            return new Result(Type.Fail, Global.getMessages().get("tpa.request.blocked.other", "player", target.getDisplayName()));
        }
        
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        ProxiedPlayer pTarget = proxy.getPlayer(target.getUniqueId());
        
        if (!isTPAWhitelisted(pTarget.getServer(), pPlayer.getServer())) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.server"));
        }
        
        ScheduledTask expireTask = ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                if (targetTp.getTPAHere() != null) {
                    if (!targetTp.getTPAHere().equals(player)) {
                        return;
                    }
                    
                    sendMessage(player, Global.getMessages().get("tpahere.request.timeout", "player", target.getDisplayName()));
                    targetTp.setTPAHere(null, null);
                    
                    sendMessage(target, Global.getMessages().get("tpa.request.timeout.other", "player", player.getDisplayName()));
                }
            }
        }, tpaExpireTime, TimeUnit.SECONDS);
        
        targetTp.setTPAHere(player, expireTask);
        sendMessage(target, Global.getMessages().get("tpahere.request.receive", "player", player.getDisplayName()));
        
        return new Result(Type.Success, Global.getMessages().get("tpa.request.sent", "player", player.getDisplayName()));
    }

    @Override
    public Result tpall(GlobalPlayer target) {
        if (!isOnline(target)) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", target.getDisplayName()));
        }

        String message = Global.getMessages().get("teleport.all", "player", target.getDisplayName());
        for (ProxiedPlayer pPlayer : proxy.getPlayers()) {
            if (pPlayer.getUniqueId().equals(target.getUniqueId())) {
                continue;
            }
            
            teleportTo(Global.getPlayer(pPlayer.getUniqueId()), target, true);
            pPlayer.sendMessage(message);
        }
        
        return new Result(Type.Success, null);
    }

    @Override
    public Result requestTp(final GlobalPlayer player, final GlobalPlayer target, boolean hasBypass) {
        final TeleportsAttachment playerTp = getAttachment(player);
        final TeleportsAttachment targetTp = getAttachment(target);
        
        if (playerTp.getTPA() != null && playerTp.getTPAHere() != null) {
            return new Result(Type.Fail, Global.getMessages().get("tpa.request.blocked"));
        }
        
        if (!isOnline(target)) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", target.getDisplayName()));
        }
        
        if (player.equals(target)) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.player", "player", player.getDisplayName()));
        }
        
        if (!hasBypass && !target.hasTPsEnabled()) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.player", "player", target.getDisplayName()));
        }
        
        if (targetTp.getTPA() != null && targetTp.getTPAHere() != null) {
            return new Result(Type.Fail, Global.getMessages().get("tpa.request.blocked.other", "player", target.getDisplayName()));
        }
        
        // Cross TPA server whitelist handling
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        ProxiedPlayer pTarget = proxy.getPlayer(target.getUniqueId());
        
        if (!isTPAWhitelisted(pPlayer.getServer(), pTarget.getServer())) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.server"));
        }
        
        ScheduledTask expireTask = ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
            @Override
            public void run() {
                if (targetTp.getTPA() != null) {
                    if (!targetTp.getTPA().equals(player)) {
                        return;
                    }
                    
                    sendMessage(player, Global.getMessages().get("tpa.request.timeout", "player", target.getDisplayName()));
                    targetTp.setTPA(null, null);
                    
                    sendMessage(target, Global.getMessages().get("tpa.request.timeout.other", "player", "{player}", player.getDisplayName()));
                }
            }
        }, tpaExpireTime, TimeUnit.SECONDS);
        
        targetTp.setTPA(player, expireTask);
        sendMessage(target, Global.getMessages().get("tpa.request.receive", "player", player.getDisplayName()));
        
        return new Result(Type.Success, ChatColor.translateAlternateColorCodes('&', Global.getMessages().get("tpa.request.sent", "player", target.getDisplayName())));
    }

    @Override
    public Result acceptTp(GlobalPlayer player) {
        TeleportsAttachment attachment = getAttachment(player);
        if (attachment.getTPA() != null) {
            GlobalPlayer target = attachment.getTPA();
            sendMessage(player, Global.getMessages().get("tpa.request.accept.you", "player", target.getDisplayName()));
            sendMessage(target, Global.getMessages().get("tpa.request.accept.them", "player", player.getDisplayName()));
            teleport0(target, player);
            attachment.setTPA(null, null);
        } else if (attachment.getTPAHere() != null) {
            GlobalPlayer target = attachment.getTPAHere();
            sendMessage(player, Global.getMessages().get("tpa.request.accept.you", "player", target.getDisplayName()));
            sendMessage(target, Global.getMessages().get("tpa.request.accept.them", "player", player.getDisplayName()));
            teleport0(player, target);
            attachment.setTPAHere(null, null);
        } else {
            return new Result(Type.Fail, Global.getMessages().get("tpa.request.none"));
        }
        
        return new Result(Type.Success, null);
    }

    @Override
    public Result rejectTp(GlobalPlayer player) {
        TeleportsAttachment attachment = getAttachment(player);
        if (attachment.getTPA() != null) {
            GlobalPlayer target = attachment.getTPA();
            sendMessage(player, Global.getMessages().get("tpa.request.denied.you", "player", target.getDisplayName()));
            sendMessage(target, Global.getMessages().get("tpa.request.denied.them", "player", player.getDisplayName()));
            attachment.setTPA(null, null);
        } else if (attachment.getTPAHere() != null) {
            GlobalPlayer target = attachment.getTPAHere();
            sendMessage(player, Global.getMessages().get("tpa.request.denied.you", "player", target.getDisplayName()));
            sendMessage(target, Global.getMessages().get("tpa.request.denied.them", "player", player.getDisplayName()));
            attachment.setTPAHere(null, null);
        } else {
            return new Result(Type.Fail, Global.getMessages().get("tpa.request.none"));
        }
        
        return new Result(Type.Success, null);
    }

    @Override
    public Result toggleTp(GlobalPlayer player) {
        if (player.hasTPsEnabled()) {
            player.setTPsEnabled(false);
            return new Result(Type.Success, Global.getMessages().get("teleport.toggle.off"));
        } else {
            player.setTPsEnabled(true);
            return new Result(Type.Success, Global.getMessages().get("teleport.toggle.off"));
        }
    }

    @Override
    public Result teleport(GlobalPlayer player, Location target) {
        ProxiedPlayer sourcePlayer = ProxyServer.getInstance().getPlayer(player.getUniqueId());
        
        if (sourcePlayer == null) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", player.getDisplayName()));
        }
        
        // Check the server exists
        if (target.getServer() != null) {
            if (proxy.getServerInfo(target.getServer()) == null) {
                return new Result(Type.Fail, Global.getMessages().get("teleport.error.server", "server", target.getServer()));
            }
        }
        
        teleportTo(player, target, true);
        
        sendMessage(player, Global.getMessages().get("teleport.outgoing.loc"));
        return new Result(Type.Success, Global.getMessages().get("teleport.message.player", "player", player.getDisplayName(), "target", target.toString()));
    }
    
    public void teleportTo(GlobalPlayer player, Location target, boolean safe) {
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        
        // Sanity check
        Preconditions.checkNotNull(pPlayer, "Teleport player " + player.getName() + " was not online");
        
        ServerInfo sourceServer = pPlayer.getServer().getInfo();
        teleportToInConnection(pPlayer, target, sourceServer, safe);
    }
    
    public void teleportToInConnection(ProxiedPlayer player, Location target, ServerInfo sourceServer, boolean safe) {
        ServerInfo destServer = sourceServer;
        if (target.getServer() != null) {
            destServer = proxy.getServerInfo(target.getServer());
        }
        
        // Sanity check
        Preconditions.checkNotNull(destServer, "Teleport target server " + target.getServer() + " does not exist");
        
        TeleportMessage message = new TeleportMessage(player.getUniqueId(), target, 1, safe);
        channel.send(message, getServerId(destServer));
        
        // Move the player to the target server if needed
        if (!sourceServer.equals(destServer)) {
            player.connect(destServer);
        }
    }
    
    private void teleport0(GlobalPlayer player, Location target) {
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        ServerInfo destServer = pPlayer.getServer().getInfo();
        if (target.getServer() != null) {
            destServer = proxy.getServerInfo(target.getServer());
        }
        
        // Send request to bukkit side to handle teleport delays
        channel.send(new TeleportRequestMessage(player.getUniqueId(), target, 1), getServerId(pPlayer.getServer().getInfo()));
    }

    @Override
    public Result teleport(GlobalPlayer player, GlobalPlayer target, boolean silent, boolean hasBypass) {
        ProxiedPlayer sourcePlayer = ProxyServer.getInstance().getPlayer(player.getUniqueId());
        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(target.getUniqueId());
        
        if (sourcePlayer == null) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", player.getDisplayName()));
        }
        
        if (targetPlayer == null) {
            return new Result(Type.Fail, Global.getMessages().get("player.not-online", "player", target.getDisplayName()));
        }
        
        // Dont allow tp to players without TPs enabled
        if (!hasBypass) {
            if (!player.hasTPsEnabled() || !target.hasTPsEnabled()) {
                return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.player", "player", target.getDisplayName()));
            }
        }
        
        // Dont allow tp to self
        if (player.equals(target)) {
            return new Result(Type.Fail, Global.getMessages().get("teleport.blocked.player", "player", target.getDisplayName()));
        }
        
        teleportTo(player, target, true);
        
        if (!silent) {
            sendMessage(target, Global.getMessages().get("teleport.incoming", "player", player.getDisplayName()));
        }
        
        sendMessage(player, Global.getMessages().get("teleport.outgoing", "player", target.getDisplayName()));
        return new Result(Type.Success, Global.getMessages().get("teleport.message.player", "player", player.getDisplayName(), "target", target.getDisplayName()));
    }
    
    private void teleport0(GlobalPlayer player, GlobalPlayer target) {
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        ProxiedPlayer pTarget = proxy.getPlayer(target.getUniqueId());
        
        // Send request to bukkit side to handle teleport delays
        channel.send(new TeleportRequestMessage(player.getUniqueId(), target.getUniqueId(), 1), getServerId(pPlayer.getServer().getInfo()));
    }
    
    public void teleportTo(GlobalPlayer player, GlobalPlayer target, boolean safe) {
        ProxiedPlayer pPlayer = proxy.getPlayer(player.getUniqueId());
        ProxiedPlayer pTarget = proxy.getPlayer(target.getUniqueId());
        
        // Sanity checks
        Preconditions.checkNotNull(pPlayer, "Teleport player " + player.getName() + " was not online");
        Preconditions.checkNotNull(pTarget, "Teleport target " + target.getName() + " was not online");
        
        ServerInfo sourceServer = pPlayer.getServer().getInfo();
        ServerInfo destServer = pTarget.getServer().getInfo();
        
        TeleportMessage message = new TeleportMessage(player.getUniqueId(), target.getUniqueId(), 1, safe);
        channel.send(message, getServerId(destServer));
        
        // Move the player to the target server if needed
        if (!sourceServer.equals(destServer)) {
            pPlayer.connect(destServer);
        }
    }
    
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof TeleportMessage) {
            TeleportMessage message = (TeleportMessage)value;
            
            ProxiedPlayer player = proxy.getPlayer(message.player);
            if (player == null) {
                return;
            }
            
            ServerInfo sourceServer = player.getServer().getInfo();
            ServerInfo targetServer = sourceServer;
            
            // Bounce the message to the required other server
            if (message.targetLocation != null) {
                int targetServerId = sourceId;
                if (message.targetLocation.getServer() != null) {
                    ServerInfo server = proxy.getServerInfo(message.targetLocation.getServer());
                    if (server == null) {
                        return;
                    }
                    targetServerId = getServerId(server);
                    targetServer = server;
                }
                
                channel.send(message, targetServerId);
            } else if (message.targetPlayer != null) {
                ProxiedPlayer target = proxy.getPlayer(message.targetPlayer);
                if (target == null) {
                    return;
                }
                
                targetServer = target.getServer().getInfo();
                
                channel.send(message, getServerId(target.getServer().getInfo()));
            } else if (message.targetServer != null) {
                targetServer = proxy.getServerInfo(message.targetServer);
            } else {
                throw new IllegalArgumentException("Expected target location or player for teleport message");
            }
            
            // Move the player to the target server if needed
            if (!sourceServer.equals(targetServer)) {
                player.connect(targetServer);
            }
        } else if (value instanceof UpdateBackMessage) {
            UpdateBackMessage message = (UpdateBackMessage)value;
            
            ProxiedPlayer pPlayer = proxy.getPlayer(message.player);
            if (pPlayer == null) {
                return;
            }
            
            ServerInfo sourceServer = getServer(sourceId);
            message.location.setServer(sourceServer.getName());
            
            GlobalPlayer player = Global.getPlayer(message.player);
            
            TeleportsAttachment attachment = getAttachment(player);
            if (message.isDeath) {
                attachment.setLastDeath(message.location);
            } else {
                attachment.setLastTeleport(message.location);
            }
        }
    }
}
