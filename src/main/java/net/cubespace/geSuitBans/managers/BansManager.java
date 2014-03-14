package net.cubespace.geSuitBans.managers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.cubespace.geSuitBans.geSuitBans;
import net.cubespace.geSuitBans.tasks.PluginMessageTask;
import net.cubespace.geSuitBans.utils.TimeParser;
import org.bukkit.ChatColor;


public class BansManager {

    public static void banPlayer( String sender, String player, String msg ) {
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
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );
    }

    public static void kickAll( String sender, String msg ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "KickAll" );
            out.writeUTF( sender );
            out.writeUTF( msg );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );

    }

    public static void kickPlayer( String sender, String player, String msg ) {
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
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );

    }

    public static void tempBanPlayer( String sender, String player, String timing, String reason ) {
        int seconds = TimeParser.parseString(timing);

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "TempBanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
            out.writeInt( seconds );
            out.writeUTF( reason );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );
    }

    public static void unbanPlayer( String sender, String player ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "UnbanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );

    }

    public static void reloadBans( String string ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "ReloadBans" );
            out.writeUTF( string );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );
    }

    public static void ipBanPlayer( String sender, String player, String msg ) {
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
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );

    }

    public static void unipBanPlayer( String sender, String player, String msg ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "UnIPBanPlayer" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );

    }

    public static void checkPlayerBans( String sender, String player ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "CheckPlayerBans" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );
    }
    
    public static void displayPlayerBanHistory( String sender, String player ) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "DisplayPlayerBanHistory" );
            out.writeUTF( sender );
            out.writeUTF( player );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );
    }


    public static void sendVersion() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream( b );
        try {
            out.writeUTF( "SendVersion" );
            out.writeUTF( ChatColor.RED + "Bans - " + ChatColor.GOLD + geSuitBans.instance.getDescription().getVersion() );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        new PluginMessageTask( b ).runTaskAsynchronously( geSuitBans.instance );
    }
}
