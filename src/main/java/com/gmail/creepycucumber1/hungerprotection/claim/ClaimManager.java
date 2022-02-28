package com.gmail.creepycucumber1.hungerprotection.claim;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.event.PacketManager;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.checkerframework.checker.units.qual.A;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClaimManager {

    private HungerProtection plugin;

    public ClaimManager(HungerProtection plugin) {
        this.plugin = plugin;
    }

    public void createNewClaim(int x1, int y1, int x2, int y2, boolean isAdmin, String worldName, Player owner) {
        //--perform checks--

        //overlaps another claim
        World world = Bukkit.getWorld(worldName);
        BoundingBox box = new BoundingBox(x1, y1, world.getMinHeight(), x2, y2, world.getMaxHeight());
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims");
        for(String key : cfg.getKeys(false)) {
            if(!getWorld(key).equals(world)) return;
            if(box.overlaps((BoundingBox) cfg.get(key + ".boundingBox"))) {
                owner.sendMessage(TextUtil.convertColor("&cThis selection overlaps an existing claim."));
                PacketManager.highlightClaim(owner, key, true);
                return;
            }
        }

        //price management
        int size = Math.abs(x2 - x1) * Math.abs(y2 - y1);
        if(plugin.getPlayerManager().getClaimBlocks(owner) < size) {
            owner.sendMessage(TextUtil.convertColor("&cYou need " + (size - plugin.getPlayerManager().getClaimBlocks(owner)) + " more claim blocks to claim this area."));
            //todo send clickable message to open claim blocks gui
            return;
        }

        //--create claim--

        //generate unique identifier
        String claimID = String.valueOf((int) (Math.random() * Integer.MAX_VALUE));
        //created
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");
        String created = dateFormat.format(date);
        //buildTrusted, containerTrusted, accessTrusted
        ArrayList<String> buildTrusted = new ArrayList<>();
        ArrayList<String> containerTrusted = new ArrayList<>();
        ArrayList<String> accessTrusted = new ArrayList<>();

        HashMap<String, Object> map = new HashMap<>();
        map.put("created", created);
        map.put("isAdmin", isAdmin);
        map.put("worldName", worldName);
        map.put("boundingBox", box);
        map.put("owner", owner.getUniqueId().toString());
        map.put("buildTrusted", buildTrusted);
        map.put("containerTrusted", containerTrusted);
        map.put("accessTrusted", accessTrusted);

        if(!isAdmin) plugin.getPlayerManager().removeClaimBlocks(owner, size);
        plugin.getDataManager().getConfig().createSection("claims." + claimID, map);
        plugin.getDataManager().saveConfig();

        owner.sendMessage(TextUtil.convertColor("&6You have successfully claimed this land!"));
        if(!isAdmin) owner.sendMessage(TextUtil.convertColor("You have " + plugin.getPlayerManager().getClaimBlocks(owner) + " claim blocks remaining."));
    }

    public void removeClaim(String claimID) {
        plugin.getDataManager().getConfig().set("claims." + claimID, null);
        plugin.getDataManager().saveConfig();
    }

    //getter
    public String getClaim(Location location) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims");
        for(String key : cfg.getKeys(false)) {
            if(((BoundingBox) cfg.get(key + ".boundingBox")).contains(location.toVector()) && getWorld(key).equals(location.getWorld())) {
                return key;
            }
        }
        return "none";
    }

    public boolean getIsAdmin(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return cfg.getBoolean("isAdmin");
    }

    public BoundingBox getBoundingBox(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return (BoundingBox) cfg.get("boundingBox");
    }

    public OfflinePlayer getOwner(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return Bukkit.getOfflinePlayer(UUID.fromString(cfg.getString("owner")));
    }

    public ArrayList<OfflinePlayer> getBuilders(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> builders = new ArrayList<>();
        cfg.getStringList("buildTrusted").forEach(uuidString -> {
            builders.add(Bukkit.getOfflinePlayer(UUID.fromString(uuidString)));
        });
        return builders;
    }

    public ArrayList<OfflinePlayer> getContainer(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> containers = new ArrayList<>();
        cfg.getStringList("containerTrusted").forEach(uuidString -> {
            containers.add(Bukkit.getOfflinePlayer(UUID.fromString(uuidString)));
        });
        return containers;
    }

    public ArrayList<OfflinePlayer> getAccess(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> access = new ArrayList<>();
        cfg.getStringList("accessTrusted").forEach(uuidString -> {
            access.add(Bukkit.getOfflinePlayer(UUID.fromString(uuidString)));
        });
        return access;
    }

    public boolean getHasPermission(Player p, Location loc, int requiredLevel) { // 1 = owner/all, 2 = build, 3 = container, 4 = access, 5 = none
        String claimID = getClaim(loc);

        if(claimID.equalsIgnoreCase("none")) return true; //no claim at location
        if(p.isOp()) return true; //player is operator

        int level = Integer.MAX_VALUE;

        if(getOwner(claimID).equals(p)) level = 1; //is claim owner
        else if(getBuilders(claimID).contains(p)) level = 2; //has build permission
        else if(getContainer(claimID).contains(p)) level = 3; //has container permission
        else if(getAccess(claimID).contains(p)) level = 4; //has access permission

        return level <= requiredLevel;
    }

    public World getWorld(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return Bukkit.getWorld(cfg.getString("worldName"));
    }

}
