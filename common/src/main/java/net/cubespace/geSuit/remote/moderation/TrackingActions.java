package net.cubespace.geSuit.remote.moderation;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.TimeRecord;
import net.cubespace.geSuit.core.objects.Track;
import net.cubespace.geSuit.core.storage.StorageException;

public interface TrackingActions {
    public List<Track> getHistory(UUID id) throws StorageException;
    public List<Track> getHistory(String name) throws StorageException;
    public List<Track> getHistory(InetAddress ip) throws StorageException;
    public List<Track> getNameHistory(GlobalPlayer player) throws StorageException;
    public TimeRecord getOntime(GlobalPlayer player) throws StorageException;
    public List<TimeRecord> getOntimeTop(int offset, int size) throws StorageException;
    public List<UUID> matchPlayers(String name) throws StorageException;
    public List<UUID> matchFullPlayers(String name) throws StorageException;
}
