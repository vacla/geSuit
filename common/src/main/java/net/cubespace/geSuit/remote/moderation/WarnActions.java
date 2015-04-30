package net.cubespace.geSuit.remote.moderation;

import java.util.List;
import java.util.UUID;

import net.cubespace.geSuit.core.GlobalPlayer;
import net.cubespace.geSuit.core.objects.Result;
import net.cubespace.geSuit.core.objects.WarnInfo;
import net.cubespace.geSuit.core.storage.StorageException;

public interface WarnActions {
    public Result warn(GlobalPlayer player, String reason, String by, UUID byId);
    
    public List<WarnInfo> getActiveWarnings(GlobalPlayer player) throws StorageException;
    public List<WarnInfo> getWarnings(GlobalPlayer player) throws StorageException;
}
