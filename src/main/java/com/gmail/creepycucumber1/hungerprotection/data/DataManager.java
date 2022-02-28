package com.gmail.creepycucumber1.hungerprotection.data;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class DataManager {

    private static final String CONFIG_NAME = "claimdata.yml";
    private HungerProtection plugin;
    private FileConfiguration config;
    private File configFile;

    public DataManager(HungerProtection plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        if(this.configFile == null){
            this.configFile = new File(this.plugin.getDataFolder(), CONFIG_NAME);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultStream = plugin.getResource(CONFIG_NAME);

        if(defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if(config==null) reloadConfig();

        return config;
    }

    public void saveConfig() {
        if(config == null || configFile == null) return;

        try{
            getConfig().save(configFile);
        }
        catch(IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save to config " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if(configFile==null) {
            configFile = new File(plugin.getDataFolder(), CONFIG_NAME);
        }

        if(!configFile.exists()) {
            plugin.saveResource(CONFIG_NAME, false);
        }
    }

}
