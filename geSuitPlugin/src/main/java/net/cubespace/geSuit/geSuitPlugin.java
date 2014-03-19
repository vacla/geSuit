package net.cubespace.geSuit;

import net.cubespace.lib.CubespacePlugin;

import java.io.File;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class geSuitPlugin extends CubespacePlugin {
    @Override
    public void onEnable() {
        getPermissionManager().setup();

        File moduleFolder = new File(getDataFolder(), "modules");
        if(!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }

        getModuleManager().detectModules(moduleFolder);
        getModuleManager().loadAndEnableModules();
        getPluginMessageManager("geSuit").finish();
    }
}
