package com.cookware.home.server.MediaManager;

import com.cookware.home.server.MediaManager.DataTypes.Config;
import com.cookware.home.server.MediaManager.Managers.ConfigManager;
import junit.framework.TestCase;

/**
 * Created by Kody on 4/11/2017.
 */
public class ConfigManagerTest extends TestCase {
    public void testGetConfig() throws Exception {
        String configPath = "Config/config.properties";
        Config config = new ConfigManager(configPath).getConfig();

        try{
            assert(config.databasePath == "/media/Public/Media");
            assert(config.schedulerState == "AUTO");
        }catch(AssertionError e) {
            System.out.println("\nNeed to prepare config for deployment");

            throw e;
        }
    }
}