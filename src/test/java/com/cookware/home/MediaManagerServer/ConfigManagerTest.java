package com.cookware.home.MediaManagerServer;

import com.cookware.home.MediaManagerServer.DataTypes.Config;
import com.cookware.home.MediaManagerServer.Managers.ConfigManager;
import junit.framework.TestCase;

/**
 * Created by Kody on 4/11/2017.
 */
public class ConfigManagerTest extends TestCase {
    public void testGetConfig() throws Exception {
        String configPath = "Config/config.properties";
        Config config = new ConfigManager(configPath).getConfig();

        try{
//            assert(config.finalPath.equals("/media/Public/Media"));
//            assert(config.schedulerState.equals("AUTO"));
        }catch(AssertionError e) {
            System.out.println("\nNeed to prepare config for deployment");

            throw e;
        }
    }
}