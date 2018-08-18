package net.cubespace.geSuit.configs;

import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 24/09/2017.
 */
public class MainConfigTest {
    File testFile = new File("target/tests/newConfig.yml");

    @Test
    public void yamlTest() throws Exception {
        MainConfig testConfig = new MainTestConfig(testFile);
        testConfig.init();
        testConfig.save();
        testConfig.MOTD_Enabled = !testConfig.MOTD_Enabled;
        MainConfig newConfig = new MainTestConfig(testFile);
        newConfig.init();
        newConfig.load(testFile);
        assertEquals(newConfig.Seen_Enabled, testConfig.Seen_Enabled);
        assertNotEquals(newConfig.MOTD_Enabled, testConfig.MOTD_Enabled);
    }

    @Test
    public void load() {
    }

    @After
    public void tearDown() {
        testFile.delete();
    }

    private class MainTestConfig extends MainConfig {

        private MainTestConfig(File file) {
            super(file);
        }
    }

}