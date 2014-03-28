package net.cubespace.geSuit.Database.Repository;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class PlayerRepository {
    public static void createPlayer(ProxiedPlayer proxiedPlayer) {
        Deferred<Boolean, Throwable, Void> createPlayerDeferred = new DeferredObject<>();


    }
}
