package com.gmail.creepycucumber1.hungerprotection.claim;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerManager {

    private HungerProtection plugin;

    public PlayerManager(HungerProtection plugin) {
        this.plugin = plugin;
    }

    public void createNewPlayer(Player player) {
        String uuid = player.getUniqueId().toString();

        plugin.getDataManager().getConfig().createSection("players." + uuid);

        HashMap<String, Object> map = new HashMap<>();
        map.put("claimBlocks", 2000);
        map.put("accruedBlocks", 0);

        plugin.getDataManager().getConfig().createSection("players." + uuid, map);
        plugin.getDataManager().saveConfig();
    }

    //setter
    public void addClaimBlocks(Player player, int blocks) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("claimBlocks", cfg.getInt("claimBlocks") + blocks);
        plugin.getDataManager().saveConfig();
    }

    public void removeClaimBlocks(Player player, int blocks) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("claimBlocks", cfg.getInt("claimBlocks") - blocks);
        plugin.getDataManager().saveConfig();
    }

    public void addAccruedBlocks(Player player, int blocks) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("accruedBlocks", cfg.getInt("accruedBlocks") + blocks);
        plugin.getDataManager().saveConfig();
    }

    //getter
    public int getClaimBlocks(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getInt("claimBlocks");
    }

    public int getAccruedBlocks(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getInt("accruedBlocks");
    }

}
