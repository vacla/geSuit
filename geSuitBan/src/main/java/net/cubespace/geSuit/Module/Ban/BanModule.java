package net.cubespace.geSuit.Module.Ban;

import net.cubespace.geSuit.Module.Ban.Config.BansConfig;
import net.cubespace.lib.Module.Module;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class BanModule extends Module {
    @Override
    public void onLoad() {
        getConfigManager().registerConfig("bans", BansConfig.class);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
