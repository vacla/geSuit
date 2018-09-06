package net.cubespace.geSuit.managers;

import com.google.common.collect.Lists;
import net.cubespace.geSuit.Utilities;
import net.cubespace.geSuit.geSuit;
import net.cubespace.geSuit.objects.Track;
import net.cubespace.geSuit.tasks.SendPluginMessage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class APIManager {
    
    public static void doResolveNames(final ServerInfo server, final int id, String strList) {
        final String[] names = strList.split(";");

        ProxyServer.getInstance().getScheduler().runAsync(geSuit.getInstance(), () -> {
            try {
                Map<String, UUID> results = resolveNames(Arrays.asList(names));

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                out.writeUTF("PlayerNameToUUID");
                out.writeInt(id);

                StringBuilder builder = new StringBuilder();
                for (Entry<String, UUID> result : results.entrySet()) {
                    if (builder.length() != 0) {
                        builder.append(';');
                    }

                    builder.append(result.getKey());
                    builder.append(':');
                    builder.append(result.getValue().toString());
                }

                out.writeUTF(builder.toString());

                new SendPluginMessage("geSuitAPI", server, stream).run();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }
    
    public static Map<String, UUID> resolveNames(List<String> names) {
        return DatabaseManager.players.resolvePlayerNames(names);
    }
    
    public static void doResolveIDs(final ServerInfo server, final int id, String strList) {
        String[] rawIds = strList.split(";");
        
        final List<UUID> ids = Lists.newArrayListWithExpectedSize(rawIds.length);
        for (String rawId : rawIds) {
            ids.add(Utilities.makeUUID(rawId));
        }

        ProxyServer.getInstance().getScheduler().runAsync(geSuit.getInstance(), () -> {
            try {
                Map<UUID, String> results = APIManager.resolveIds(ids);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                out.writeUTF("UUIDToPlayerName");
                out.writeInt(id);

                StringBuilder builder = new StringBuilder();
                for (Entry<UUID, String> result : results.entrySet()) {
                    if (builder.length() != 0) {
                        builder.append(';');
                    }

                    builder.append(result.getKey().toString());
                    builder.append(':');
                    builder.append(result.getValue());
                }

                out.writeUTF(builder.toString());

                new SendPluginMessage("geSuitAPI", server, stream).run();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }
    
    public static Map<UUID, String> resolveIds(List<UUID> ids) {
        return DatabaseManager.players.resolveUUIDs(ids);
    }
    
    public static void doNameHistory(final ServerInfo server, final int id, String raw) {
        final UUID uuid = Utilities.makeUUID(raw);

        ProxyServer.getInstance().getScheduler().runAsync(geSuit.getInstance(), () -> {
            try {
                List<Track> results = APIManager.getNameHistory(uuid);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                out.writeUTF("PlayerNameHistory");
                out.writeInt(id);

                StringBuilder builder = new StringBuilder();
                for (Track result : results) {
                    if (builder.length() != 0) {
                        builder.append(';');
                    }

                    builder.append(result.getPlayer());
                    builder.append(':');
                    builder.append(result.getLastSeen().getTime());
                }

                out.writeUTF(builder.toString());

                new SendPluginMessage("geSuitAPI", server, stream).run();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        });
    }
    
    public static List<Track> getNameHistory(UUID id) {
        return DatabaseManager.tracking.getNameHistory(id);
    }
}
