package net.cubespace.geSuit.moderation;

import net.cubespace.geSuit.GSPlugin;
import net.cubespace.geSuit.core.Global;
import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.channel.Channel;
import net.cubespace.geSuit.core.channel.ChannelDataReceiver;
import net.cubespace.geSuit.core.commands.CommandManager;
import net.cubespace.geSuit.core.events.moderation.GlobalBanEvent;
import net.cubespace.geSuit.core.events.moderation.GlobalUnbanEvent;
import net.cubespace.geSuit.core.events.moderation.GlobalWarnEvent;
import net.cubespace.geSuit.core.messages.BaseMessage;
import net.cubespace.geSuit.core.messages.FireBanEventMessage;
import net.cubespace.geSuit.core.messages.FireWarnEventMessage;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.moderation.commands.BanCommands;
import net.cubespace.geSuit.moderation.commands.KickCommands;
import net.cubespace.geSuit.moderation.commands.LookupCommands;
import net.cubespace.geSuit.moderation.commands.MuteCommands;
import net.cubespace.geSuit.moderation.commands.WarnCommands;
import net.cubespace.geSuit.modules.BaseModule;
import net.cubespace.geSuit.modules.Module;
import net.cubespace.geSuit.remote.moderation.BanActions;
import net.cubespace.geSuit.remote.moderation.MuteActions;
import net.cubespace.geSuit.remote.moderation.TrackingActions;
import net.cubespace.geSuit.remote.moderation.WarnActions;

@Module(name="Moderation")
public class ModeraionModule extends BaseModule implements ChannelDataReceiver<BaseMessage> {
    
    private BanActions bans;
    private WarnActions warns;
    private TrackingActions tracking;
    private MuteActions mutes;
    private Channel<BaseMessage> channel;
    
    public ModeraionModule(GSPlugin plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onLoad() throws Exception {
        Global.getRemoteManager().registerInterest("bans", BanActions.class);
        Global.getRemoteManager().registerInterest("warns", WarnActions.class);
        Global.getRemoteManager().registerInterest("tracking", TrackingActions.class);
        Global.getRemoteManager().registerInterest("mutes", MuteActions.class);
        
        bans = Global.getRemoteManager().getRemote(BanActions.class);
        warns = Global.getRemoteManager().getRemote(WarnActions.class);
        tracking = Global.getRemoteManager().getRemote(TrackingActions.class);
        mutes = Global.getRemoteManager().getRemote(MuteActions.class);
        
        return true;
    }
    
    @Override
    public boolean onEnable() throws Exception {
        channel = Global.getChannelManager().createChannel("moderation", BaseMessage.class);
        channel.setCodec(new BaseMessage.Codec());
        channel.addReceiver(this);
        
        return true;
    }
    
    @Override
    public void onDisable(DisableReason reason) throws Exception {
    }
    
    @Override
    public void registerCommands(CommandManager manager) {
        manager.registerAll(new BanCommands(bans), getPlugin());
        manager.registerAll(new WarnCommands(warns), getPlugin());
        manager.registerAll(new KickCommands(bans), getPlugin());
        manager.registerAll(new LookupCommands(tracking), getPlugin());
        manager.registerAll(new MuteCommands(mutes), getPlugin());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void onDataReceive(Channel<BaseMessage> channel, BaseMessage value, int sourceId, boolean isBroadcast) {
        if (value instanceof FireBanEventMessage) {
            FireBanEventMessage message = (FireBanEventMessage)value;
            
            if (message.isUnban) {
                if (message.address != null) {
                    Global.getPlatform().callEvent(new GlobalUnbanEvent((BanInfo<GlobalPlayer>)message.ban, message.address));
                } else {
                    Global.getPlatform().callEvent(new GlobalUnbanEvent(message.ban));
                }
            } else {
                if (message.address != null) {
                    Global.getPlatform().callEvent(new GlobalBanEvent((BanInfo<GlobalPlayer>)message.ban, message.address, message.auto));
                } else {
                    Global.getPlatform().callEvent(new GlobalBanEvent(message.ban, message.auto));
                }
            }
        } else if (value instanceof FireWarnEventMessage) {
            FireWarnEventMessage message = (FireWarnEventMessage)value;
            
            Global.getPlatform().callEvent(new GlobalWarnEvent(message.warn, message.action, message.number));
        }
    }
}
