package net.cubespace.geSuit.moderation;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.cubespace.geSuit.config.ConfigManager;
import net.cubespace.geSuit.config.ConfigReloadListener;
import net.cubespace.geSuit.config.ModerationConfig;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.events.moderation.GlobalWarnEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.FireWarnEventMessage;
import net.cubespace.geSuit.core.objects.DateDiff;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.WarnAction;
import net.cubespace.geSuit.core.objects.WarnAction.ActionType;
import net.cubespace.geSuit.core.objects.WarnInfo;
import net.cubespace.geSuit.core.objects.Result.Type;
import net.cubespace.geSuit.core.storage.StorageException;
import net.cubespace.geSuit.database.repositories.WarnHistory;
import net.cubespace.geSuit.general.BroadcastManager;
import net.cubespace.geSuit.remote.moderation.WarnActions;
import net.md_5.bungee.api.ChatColor;

import com.google.common.base.Preconditions;

public class WarningsManager implements WarnActions, ConfigReloadListener {
    private Logger logger;
    private WarnHistory warnRepo;
    private BanManager banManager;
    private BroadcastManager broadcasts;
    private WarnAction[] actions;
    private Channel<BaseMessage> channel;
    
    private long warnExpiryTime;
    private boolean broadcastWarns;
    private String defaultReason;
    
    public WarningsManager(WarnHistory warnRepo, BanManager banManager, BroadcastManager broadcasts, Channel<BaseMessage> channel, Logger logger) {
        this.warnRepo = warnRepo;
        this.banManager = banManager;
        this.broadcasts = broadcasts;
        this.channel = channel;
        this.logger = logger;
    }
    
    public void loadConfig(ModerationConfig config) {
        warnExpiryTime = TimeUnit.DAYS.toMillis(config.WarningExpiryDays);
        broadcastWarns = config.BroadcastWarns;
        defaultReason = config.DefaultWarnReason;
        
        Map<Integer, String> defs = config.Actions;
        if (defs == null) {
            actions = new WarnAction[0];
        }
        
        int max = 0;
        for (Integer val : defs.keySet()) {
            Preconditions.checkArgument(val > 0, "Illegal action number " + val + ". Numbers must be 1 or greater");
            if (val > max) {
                max = val;
            }
        }
        
        actions = new WarnAction[max];
        // Fill array with what we have
        for (Entry<Integer, String> entry : defs.entrySet()) {
            try {
                WarnAction action = parseAction(entry.getValue());
                actions[entry.getKey()-1] = action;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage() + " on action " + entry.getKey());
            }
        }
        
        // Fill blanks
        WarnAction last = null;
        for (int i = 0; i < actions.length; ++i) {
            if (actions[i] == null) {
                if (last == null) {
                    last = new WarnAction(ActionType.None);
                }
                actions[i] = last;
            } else {
                last = actions[i];
            }
        }
    }
    
    @Override
    public void onConfigReloaded(ConfigManager manager) {
        loadConfig(manager.moderation());
    }
    
    private WarnAction parseAction(String def) {
        String[] parts = def.toLowerCase().split(" ");
        
        if (parts.length > 2) {
            throw new IllegalArgumentException("Bad warn action definition. Expected '<action> [time]', got " + def);
        }
        
        long time = 0;
        if (parts.length == 2) {
            try {
                DateDiff diff = DateDiff.valueOf(parts[1]);
                time = diff.toMillis();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Time format for action " + parts[0] + " was incorrect.");
            }
        }
        
        switch (parts[0]) {
        case "none":
            return new WarnAction(ActionType.None);
        case "kick":
            return new WarnAction(ActionType.Kick);
        case "mute":
            Preconditions.checkArgument(time != 0, "Expected time to be specified for action 'mute'");
            return new WarnAction(ActionType.Mute, time);
        case "ban":
            return new WarnAction(ActionType.Ban);
        case "tempban":
            Preconditions.checkArgument(time != 0, "Expected time to be specified for action 'tempban'");
            return new WarnAction(ActionType.TempBan, time);
        case "ipban":
            return new WarnAction(ActionType.IPBan);
        case "tempipban":
            Preconditions.checkArgument(time != 0, "Expected time to be specified for action 'tempipban'");
            return new WarnAction(ActionType.TempIPBan, time);
        default:
            throw new IllegalArgumentException("Unknown action type " + parts[0]);
        }
    }
    
    public WarnAction getAction(int warnNumber) {
        Preconditions.checkArgument(warnNumber > 0);
        
        if (actions.length == 0) {
            return new WarnAction(ActionType.None);
        } else if (warnNumber > actions.length) {
            return actions[actions.length-1];
        } else {
            return actions[warnNumber-1];
        }
    }
    
    public boolean hasActions() {
        return actions.length != 0;
    }
    
    @Override
    public Result warn(GlobalPlayer player, String reason, String by, UUID byId) {
        try {
            if (reason == null) {
                reason = defaultReason;
            }
            
            // Record warning
            WarnInfo warning = new WarnInfo(
                    player, 
                    reason, 
                    by, 
                    byId, 
                    System.currentTimeMillis(), 
                    System.currentTimeMillis() + warnExpiryTime
                    );
            
            warnRepo.recordWarn(warning);
            
            List<WarnInfo> warnings = warnRepo.getActiveWarnings(player);
            // Perform actions
            WarnAction action = getAction(warnings.size());
            
            switch (action.getType()) {
            case Mute:
                throw new UnsupportedOperationException("Not yet implemented");
            case Kick:
                banManager.kick(player, reason, true);
                break;
            case TempBan:
                banManager.banUntil(player, reason, System.currentTimeMillis() + action.getTime(), by, byId, true);
                break;
            case Ban:
                banManager.ban(player, reason, by, byId, true);
                break;
            case IPBan:
                banManager.ipban(player, reason, by, byId, true);
                break;
            case TempIPBan:
                banManager.ipbanUntil(player, reason, System.currentTimeMillis() + action.getTime(), by, byId, true);
                break;
            default:
                break;
            }
            
            Global.getPlatform().callEvent(new GlobalWarnEvent(warning, action, warnings.size()));
            channel.broadcast(new FireWarnEventMessage(warning, action, warnings.size()));
            
            // Broadcast
            String message = Global.getMessages().get("warn.display.broadcast", "player", player.getDisplayName(), "message", reason, "sender", by);
            if (broadcastWarns) {
                broadcasts.broadcastGlobal(message);
                return new Result(Type.Success, null);
            } else {
                return new Result(Type.Success, message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "A database exception occured while attempting to warn " + player.getDisplayName(), e);
            return new Result(Type.Fail, ChatColor.RED + "An internal error occured");
        }
    }
    
    @Override
    public List<WarnInfo> getActiveWarnings(GlobalPlayer player) throws StorageException {
        try {
            return warnRepo.getActiveWarnings(player);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get active warns for " + player.getDisplayName(), e);
            throw new StorageException("Unable to retrieve active warnings");
        }
    }
    
    @Override
    public List<WarnInfo> getWarnings(GlobalPlayer player) throws StorageException {
        try {
            return warnRepo.getWarnHistory(player);
        } catch (SQLException e) {
            logger.log(Level.SEVERE,  "A database exception occured while attempting to get warnhistory for " + player.getDisplayName(), e);
            throw new StorageException("Unable to retrieve warnhistory");
        }
    }
}
