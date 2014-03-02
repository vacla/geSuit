package net.cubespace.geSuit.objects;

import net.cubespace.geSuit.Utilities;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class GSPlayer {
    private String playername;
    private String uuid;
    private boolean acceptingTeleports;

    private HashMap<String, ArrayList<Home>> homes = new HashMap<>();
    private Location deathBackLocation;
    private Location teleportBackLocation;
    private boolean lastBack;
    private boolean firstConnect = true;

    public GSPlayer(String name, String uuid, boolean tps) {
        this.playername = name;
        this.uuid = uuid;
        this.acceptingTeleports = tps;
    }

    public String getName() {
        return playername;
    }

    public void setPlayerName( String name ) {
        this.playername = name;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return ProxyServer.getInstance().getPlayer( playername );
    }

    public void sendMessage( String message ) {
        for ( String line : message.split( "\n" ) ) {
            getProxiedPlayer().sendMessage( Utilities.colorize(line) );
        }
    }

    public boolean acceptingTeleports() {
        return this.acceptingTeleports;
    }

    public void setAcceptingTeleports( boolean tp ) {
        this.acceptingTeleports = tp;
    }

    public void setDeathBackLocation( Location loc ) {
        deathBackLocation = loc;
        lastBack = true;
    }

    public boolean hasDeathBackLocation() {
        return deathBackLocation != null;
    }

    public void setTeleportBackLocation( Location loc ) {
        teleportBackLocation = loc;
        lastBack = false;
    }

    public Location getLastBackLocation() {
        if ( lastBack ) {
            return deathBackLocation;
        } else {
            return teleportBackLocation;
        }
    }

    public boolean hasTeleportBackLocation() {
        return teleportBackLocation != null;
    }

    public Location getDeathBackLocation() {
        return deathBackLocation;
    }

    public Location getTeleportBackLocation() {
        return teleportBackLocation;
    }

    public Server getServer() {
        return ProxyServer.getInstance().getPlayer( playername ).getServer();
    }

    public HashMap<String, ArrayList<Home>> getHomes() {
        return homes;
    }

    public boolean firstConnect() {
        return firstConnect;
    }

    public void connected() {
        firstConnect = false;
    }

    public void connectTo( ServerInfo s ) {
        getProxiedPlayer().connect( s );
    }

    public String getUuid() {
        return uuid;
    }
}
