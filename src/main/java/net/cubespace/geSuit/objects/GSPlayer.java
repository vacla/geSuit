package net.cubespace.geSuit.objects;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import net.cubespace.geSuit.Utilities;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GSPlayer
{

    private String playername;
    private String uuid;
    private boolean acceptingTeleports;
    private String server = null;
    private String ip;
    private Timestamp lastOnline;
    private Timestamp firstOnline;

    private HashMap<String, ArrayList<Home>> homes = new HashMap<>();
    private Location deathBackLocation;
    private Location teleportBackLocation;
    private boolean lastBack;
    private boolean firstConnect = true;
    private boolean joinAnnounced = false;
    private boolean isFirstJoin = false;
    private boolean newSpawn = false;
    private long loginTime;

    public GSPlayer(String name, String uuid, boolean tps)
    {
        this(name, uuid, tps, null);
    }

    public GSPlayer(String name, String uuid, boolean tps, String ip)
    {
        this(name, uuid, tps, false, ip, null, null);
    }
    
    public GSPlayer(String name, String uuid, boolean tps, boolean newspawn, String ip, Timestamp lastOnline, Timestamp firstOnline)
    {
        //ProxyServer.getInstance().getLogger().info("LOADED DATA: "+name+" "+uuid+" "+tps+" "+ip+" "+lastOnline);
        this.playername = name;
        this.uuid = uuid;
        this.acceptingTeleports = tps;
        this.ip = ip;
        this.lastOnline = lastOnline;
        this.firstOnline = firstOnline;
        this.newSpawn = newspawn;
        this.loginTime = new Date().getTime();
    }

    public String getName()
    {
        return playername;
    }

    public void setName(String newPlayerName)
    {
        playername = newPlayerName;
    }

    public ProxiedPlayer getProxiedPlayer()
    {
        return ProxyServer.getInstance().getPlayer(playername);
    }

    public void sendMessage(String message)
    {
        for (String line : message.split("\n|\\{N\\}")) {
            getProxiedPlayer().sendMessage(TextComponent.fromLegacyText(Utilities.colorize(line)));
        }
    }

    public boolean acceptingTeleports()
    {
        return this.acceptingTeleports;
    }

    public void setAcceptingTeleports(boolean tp)
    {
        this.acceptingTeleports = tp;
    }

    public void setDeathBackLocation(Location loc)
    {
        deathBackLocation = loc;
        lastBack = true;
    }

    public boolean hasDeathBackLocation()
    {
        return deathBackLocation != null;
    }

    public void setTeleportBackLocation(Location loc)
    {
        teleportBackLocation = loc;
        lastBack = false;
    }

    public Location getLastBackLocation()
    {
        if (lastBack) {
            return deathBackLocation;
        }
        else {
            return teleportBackLocation;
        }
    }

    public boolean hasTeleportBackLocation()
    {
        return teleportBackLocation != null;
    }

    public Location getDeathBackLocation()
    {
        return deathBackLocation;
    }

    public Location getTeleportBackLocation()
    {
        return teleportBackLocation;
    }

    public String getServer()
    {
        if ((getProxiedPlayer() == null) || (getProxiedPlayer().getServer() == null)){
            return server;
        }

        return getProxiedPlayer().getServer().getInfo().getName();
    }

    public HashMap<String, ArrayList<Home>> getHomes()
    {
        return homes;
    }

    public boolean firstConnect()
    {
        return firstConnect;
    }

    public void connected()
    {
        firstConnect = false;
    }

    public void connectTo(ServerInfo s)
    {
        getProxiedPlayer().connect(s);
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ipAddress)
    {
        ip = ipAddress;
    }

    public Timestamp getLastOnline()
    {
        return lastOnline;
    }
    
    public Timestamp getFirstOnline()
    {
        return firstOnline;
    }
    
    public boolean isFirstJoin()
    {
    	return isFirstJoin;
    }
    
    public void setFirstJoin(boolean value)
    {
    	isFirstJoin = value;
    }

	public boolean hasJoinAnnounced() {
		return joinAnnounced;
	}

	public void setJoinAnnounced(boolean joinAnnounced) {
		this.joinAnnounced = joinAnnounced;
	}

	public boolean isNewSpawn() {
		return newSpawn;
	}

	public void setNewSpawn(boolean newSpawn) {
		this.newSpawn = newSpawn;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
}
