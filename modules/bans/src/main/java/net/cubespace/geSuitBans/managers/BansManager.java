package net.cubespace.geSuitBans.managers;

import net.cubespace.geSuit.managers.DataManager;
import net.cubespace.geSuit.utils.Utilities;
import net.cubespace.geSuitBans.geSuitBans;
import org.bukkit.ChatColor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class BansManager extends DataManager {

    public BansManager(geSuitBans plugin) {
        super(plugin);
    }

    public void banPlayer(String sender, String player, String msg) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "BanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeUTF( msg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void forceToNewSpawn(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("NewSpawn");
            out.writeUTF(sender);
            out.writeUTF(player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void warnPlayer(String sender, String player, String msg) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "WarnPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeUTF( msg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void kickAll(String sender, String msg) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "KickAll" );
            out.writeUTF( sender );
            out.writeUTF( msg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);

    }

    public void kickPlayer(String sender, String player, String msg) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "KickPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeUTF( msg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void tempBanPlayer(String sender, String player, int seconds, String reason) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "TempBanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeInt(seconds);
            out.writeUTF( reason );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    /**
     *
     * @deprecated Does not validate timing is non-zero or a valid time.
     */
    @Deprecated
    public void tempBanPlayer(String sender, String player, String timing, String reason) {
        int seconds = Utilities.parseStringToSecs(timing);
        tempBanPlayer(sender, player, seconds, reason);
    }


    public void unbanPlayer(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "UnbanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);

    }

    public void reloadBans(String string) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "ReloadBans" );
            out.writeUTF( string );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void ipBanPlayer(String sender, String player, String msg) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "IPBanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeUTF( msg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void unipBanPlayer(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "UnIPBanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);

    }

    public void checkPlayerBans(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "CheckPlayerBans" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayPlayerBanHistory(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayPlayerBanHistory" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayPlayerWarnHistory(String sender, String player, boolean showStaffNames) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayPlayerWarnHistory" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeBoolean( showStaffNames );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayPlayerWarnBanHistory(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayPlayerWarnBanHistory" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayWhereHistory(String sender, String options, String search) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayWhereHistory" );
            out.writeUTF( sender );
            out.writeUTF( options );
            out.writeUTF( search );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayOnTimeHistory(String sender, String player) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayOnTimeHistory" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayOnTimeTop(String sender, int pagenum) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayOnTimeTop" );
            out.writeUTF( sender );
            out.writeInt(pagenum);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayLastLogins(String sender, String target, int pagenum) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayLastLogins" );
            out.writeUTF( sender );
            out.writeUTF( target );
            out.writeInt( pagenum );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void displayNameHistory(String sender, String nameOrId) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayNameHistory" );
            out.writeUTF( sender );
            out.writeUTF(nameOrId);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void lockDown(String sender, long expiryTime, String msg) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("LockDown");
            out.writeUTF(sender);
            out.writeLong(expiryTime);
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void endLockDown(String sender) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("EndLockDown");
            out.writeUTF(sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void lockDownStatus(String sender) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("LockDownStatus");
            out.writeUTF(sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }

    public void sendVersion() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SendVersion" );
            out.writeUTF(ChatColor.RED + "Bans - " + ChatColor.GOLD + instance.getDescription().getVersion());
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        instance.sendMessage(b);
    }
}
