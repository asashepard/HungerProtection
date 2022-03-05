package com.gmail.creepycucumber1.hungerprotection.claim;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
        map.put("isClaiming", false);
        map.put("activeCID", "none"); //for resizing claims
        map.put("x1", Integer.MAX_VALUE);
        map.put("z1", Integer.MAX_VALUE);
        map.put("lastTools", Instant.now().toEpochMilli() - 300000);

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

    public void setIsClaiming(Player player, boolean bool) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("isClaiming", bool);
        plugin.getDataManager().saveConfig();
    }

    public void setActiveCID(Player player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("activeCID", claimID);
        plugin.getDataManager().saveConfig();
    }

    public void setX1(Player player, int x1) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("x1", x1);
        plugin.getDataManager().saveConfig();
    }

    public void setZ1(Player player, int z1) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("z1", z1);
        plugin.getDataManager().saveConfig();
    }

    public void resetCurrentClaimingData(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        cfg.set("isClaiming", false);
        cfg.set("activeCID", "none");
        cfg.set("x1", Integer.MAX_VALUE);
        cfg.set("z1", Integer.MAX_VALUE);
        plugin.getDataManager().saveConfig();
    }

    public void setLastToolsToNow(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        long time = Instant.now().toEpochMilli();
        cfg.set("lastTools", time);
        plugin.getDataManager().saveConfig();
    }

    //getter
    public ArrayList<OfflinePlayer> getPlayers() {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players");
        ArrayList<OfflinePlayer> players = new ArrayList<>();
        for(String uuidString : cfg.getKeys(false)) {
            players.add(Bukkit.getOfflinePlayer(UUID.fromString(uuidString)));
        }
        return players;
    }

    public int getClaimBlocks(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getInt("claimBlocks");
    }

    public int getAccruedBlocks(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getInt("accruedBlocks");
    }

    public boolean isClaiming(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getBoolean("isClaiming");
    }

    public String getActiveCID(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getString("activeCID");
    }

    public int getX1(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getInt("x1");
    }

    public int getZ1(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getInt("z1");
    }

    public long getLastTools(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
        return cfg.getLong("lastTools");
    }

}
