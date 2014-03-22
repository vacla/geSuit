package net.cubespace.geSuit.Module.Metrics;

import net.cubespace.lib.Metrics;
import net.cubespace.lib.Module.Module;

import java.io.IOException;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class MetricsModule extends Module {
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        try {
            Metrics metrics = new Metrics(plugin);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
            getModuleLogger().error("Could not start Metrics", e);
        }
    }

    @Override
    public void onDisable() {
        plugin.getAsyncEventBus().removeListener(this);
    }
}
