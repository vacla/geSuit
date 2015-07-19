package net.cubespace.geSuit.moderation;

import java.util.logging.Logger;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.BanInfo;
import net.cubespace.geSuit.events.GlobalPlayerPreLoginEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BanListener implements Listener {
    private final BanManager banManager;
    private final Logger logger;
    
    public BanListener(BanManager banManager, Logger logger) {
        this.banManager = banManager;
        this.logger = logger;
    }
    
    @EventHandler
    public void doBanCheck(GlobalPlayerPreLoginEvent event) {
        GlobalPlayer player = event.getPlayer();
        
        // Check player ban status
        BanInfo<?> ban = banManager.getAnyBan(event.getPlayer());
        if (ban != null) {
            String reason = banManager.getBanKickReason(ban);
            event.denyLogin(reason);
            
            // Console log
            if (ban.isTemporary()) {
                if (ban.getWho() instanceof GlobalPlayer) {
                    logger.info(ChatColor.RED + player.getName() + "'s connection refused due to being temp banned!");
                } else {
                    logger.info(ChatColor.RED + player.getName() + "'s connection refused due to being temp ip-banned!");
                }
            } else {
                if (ban.getWho() instanceof GlobalPlayer) {
                    logger.info(ChatColor.RED + player.getName() + "'s connection refused due to being banned!");
                } else {
                    logger.info(ChatColor.RED + player.getName() + "'s connection refused due to being ip-banned!");
                }
            }
        }
    }
}
