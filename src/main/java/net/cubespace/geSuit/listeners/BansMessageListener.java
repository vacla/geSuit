package net.cubespace.geSuit.listeners;

import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.managers.BansManager;
import net.cubespace.geSuit.managers.LockDownManager;
import net.cubespace.geSuit.managers.LoggingManager;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;

public class BansMessageListener implements Listener {

    @EventHandler
    public void receivePluginMessage(PluginMessageEvent event) throws IOException, SQLException {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getSender() instanceof Server)) {
            return;
        }

        if (!event.getTag().equalsIgnoreCase("geSuitBans")) {
            return;
        }

		// Message debugging (can be toggled live)
		if (geSuit.instance.isDebugEnabled()) {
			Utilities.dumpPacket(event.getTag(), "RECV", event.getData(), true);
		}

        event.setCancelled(true);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

        String task = in.readUTF();
        switch (task) {
            case "KickPlayer":
                BansManager.kickPlayer(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "BanPlayer":
                BansManager.banPlayer(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "WarnPlayer":
                BansManager.warnPlayer(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "TempBanPlayer":
                BansManager.tempBanPlayer(in.readUTF(), in.readUTF(), in.readInt(), in.readUTF());
                break;
            case "KickAll":
                BansManager.kickAll(in.readUTF(), in.readUTF());
                break;
            case "UnbanPlayer":
                BansManager.unbanPlayer(in.readUTF(), in.readUTF());
                break;
            case "IPBanPlayer":
                BansManager.banIP(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "CheckPlayerBans":
                BansManager.checkPlayersBan(in.readUTF(), in.readUTF());
                break;
            case "DisplayPlayerBanHistory":
                BansManager.displayPlayerBanHistory(in.readUTF(), in.readUTF());
                break;
            case "DisplayPlayerWarnHistory":
            	BansManager.displayPlayerWarnHistory(in.readUTF(), in.readUTF(), in.readBoolean());
                break;
            case "DisplayWhereHistory":
                BansManager.displayWhereHistory(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "DisplayOnTimeHistory":
                BansManager.displayPlayerOnTime(in.readUTF(), in.readUTF());
                break;
            case "DisplayOnTimeTop":
                BansManager.displayOnTimeTop(in.readUTF(), in.readInt());
                break;
            case "DisplayLastLogins":
                BansManager.displayLastLogins(in.readUTF(), in.readUTF(), in.readInt());
                break;
            case "ReloadBans":
                BansManager.reloadBans(in.readUTF());
                break;
            case "SendVersion":
                LoggingManager.log(in.readUTF());
                break;
            case "DisplayNameHistory":
                BansManager.displayNameHistory(in.readUTF(), in.readUTF());
                break;
            case "LockDown":
                LockDownManager.startLockDown(in.readUTF(), in.readLong(), in.readUTF());
                break;
            case "EndLockDown":
                LockDownManager.endLockDown(in.readUTF());
                break;
            case "LockDownStatus":
                LockDownManager.checkExpiry(in.readUTF());
            default:
                return;
        }
        return;
    }

}
