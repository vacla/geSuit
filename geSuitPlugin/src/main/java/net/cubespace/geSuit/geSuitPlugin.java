package net.cubespace.geSuit;

import com.j256.ormlite.dao.DaoManager;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.cubespace.geSuit.Config.Main;
import net.cubespace.geSuit.Database.Table.Player;
import net.cubespace.lib.CubespacePlugin;
import net.cubespace.lib.Database.Database;
import net.cubespace.lib.Module.ModuleDescription;
import org.jdeferred.DeferredManager;
import org.jdeferred.impl.DefaultDeferredManager;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class geSuitPlugin extends CubespacePlugin {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    @Getter
    private DeferredManager dm = new DefaultDeferredManager(executorService);

    @Override
    public void onEnable() {
        getPermissionManager().setup();

        // Setup the Main Config
        Main config = new Main(this);
        try {
            config.init();
        } catch (InvalidConfigurationException e) {
            getPluginLogger().error("Could not init config.yml. Check the Format please", e);
            getProxy().getPluginManager().getPlugins().remove(this);
            return;
        }

        // Setup the Database
        database = new Database(this, config.Database.URL, config.Database.Username, config.Database.Password);
        try {
            database.registerDAO(DaoManager.createDao(database.getConnectionSource(), Player.class), Player.class);
        } catch (SQLException e) {
            getPluginLogger().error("Could not init Database");
            getProxy().getPluginManager().getPlugins().remove(this);
            return;
        }

        // Metrics is a build in Module which can't be disabled
        getModuleManager().registerModule(new ModuleDescription("Metrics", "net.cubespace.geSuit.Module.Metrics.MetricsModule", "1.0.0", "geNAZt", new HashSet<String>(), null, null));

        File moduleFolder = new File(getDataFolder(), "modules");
        if(!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }

        getModuleManager().detectModules(moduleFolder);
        getModuleManager().loadAndEnableModules();
        getPluginMessageManager("geSuit").finish();
    }
}
