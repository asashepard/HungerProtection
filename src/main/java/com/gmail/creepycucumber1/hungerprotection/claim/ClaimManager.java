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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClaimManager {

    private HungerProtection plugin;

    public ClaimManager(HungerProtection plugin) {
        this.plugin = plugin;
    }

    public boolean createNewClaim(int x1, int z1, int x2, int z2, boolean isAdmin, String worldName, Player owner) {
        //--perform checks--

        //overlaps another claim
        World world = Bukkit.getWorld(worldName);
        BoundingBox box = new BoundingBox(x1, z1, world.getMinHeight(), x2, z2, world.getMaxHeight());
        if(overlaps(box, world, owner, "")) return false;

        //price management
        int size = Math.abs(x2 - x1) * Math.abs(z2 - z1);
        if(plugin.getPlayerManager().getClaimBlocks(owner) < size) {
            owner.sendMessage(TextUtil.convertColor("&cYou need " + (size - plugin.getPlayerManager().getClaimBlocks(owner)) + " more claim blocks to claim this area."));
            TextUtil.sendClickableCommand(owner, TextUtil.convertColor("&6&nBuy claim blocks"), "/buyclaimblocks", "Open the claim blocks GUI");
            return false;
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
        //subdivisions
        ArrayList<Subdivision> subdivisions = new ArrayList<>();
        //explosions
        boolean explosions = false;

        HashMap<String, Object> map = new HashMap<>();
        map.put("created", created);
        map.put("isAdmin", isAdmin);
        map.put("worldName", worldName);
        map.put("boundingBox", box);
        map.put("owner", owner.getUniqueId().toString());
        map.put("buildTrusted", buildTrusted);
        map.put("containerTrusted", containerTrusted);
        map.put("accessTrusted", accessTrusted);
        map.put("subdivisions", subdivisions);
        map.put("explosions", explosions);

        if(!isAdmin) plugin.getPlayerManager().removeClaimBlocks(owner, size);
        plugin.getDataManager().getConfig().createSection("claims." + claimID, map);
        plugin.getDataManager().saveConfig();

        owner.sendMessage(TextUtil.convertColor("&6You have successfully made a claim!"));
        if(!isAdmin) owner.sendMessage(TextUtil.convertColor("You have " + plugin.getPlayerManager().getClaimBlocks(owner) + " claim blocks remaining."));
        PacketManager.highlightClaim(owner, claimID, false);

        return true;
    }

    public void removeClaim(String claimID) {
        plugin.getDataManager().getConfig().set("claims." + claimID, null);
        plugin.getDataManager().saveConfig();
    }

    public boolean overlaps(BoundingBox box, World world, Player player, String exempt) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims");
        for(String key : cfg.getKeys(false)) {
            if(!getWorld(key).equals(world)) continue;
            if(key.equalsIgnoreCase(exempt)) continue;
            if(box.overlaps((BoundingBox) cfg.get(key + ".boundingBox"))) {
                BoundingBox otherBox = (BoundingBox) cfg.get(key + ".boundingBox");
                int x1 = (int) otherBox.getMinX();
                int y1 = (int) otherBox.getMinY();
                int x2 = (int) otherBox.getMaxX();
                int y2 = (int) otherBox.getMaxY();
                player.sendMessage(TextUtil.convertColor("&cThis selection overlaps an existing claim at " +
                        "(" + x1 + ", " + y1 + ") -> (" + x2 + ", " + y2 + ")."));
                PacketManager.highlightClaim(player, key, true);
                return false;
            }
        }
        return true;
    }

    private boolean containsAllSubdivisions(BoundingBox box, ArrayList<Subdivision> subdivisions) {
        for(Subdivision s : subdivisions) {
            if(!box.contains(s.getBoundingBox()))
                return false;
        }
        return true;
    }

    //setter
    public void changeCorner(Player player, String claimID, int x1, int z1, int x2, int z2) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);

        World world = getWorld(claimID);
        BoundingBox box = (BoundingBox) cfg.get("boundingBox");

        int fX1 = 0;
        int fZ1 = 0;

        int xMax = (int) box.getMaxX();
        int zMax = (int) box.getMaxZ();
        int xMin = (int) box.getMinX();
        int zMin = (int) box.getMinZ();
        //set fX1 and fX2 to the corner opposite the one being resized
        if(xMax == x1) {
            fX1 = xMin;
            if(zMax == z1)
                fZ1 = zMin;
            else
                fZ1 = zMax;
        }
        else
            fX1 = xMax;

        BoundingBox newBox = new BoundingBox(fX1, fZ1, world.getMinHeight(), x2, z2, world.getMaxHeight());

        if(!overlaps(newBox, world, player, claimID) && containsAllSubdivisions(newBox, (ArrayList<Subdivision>) cfg.getList("subdivisions")))
            cfg.set("boundingBox", newBox);

        plugin.getDataManager().saveConfig();
    }

    public void setExplosions(String claimID, boolean explosions) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        cfg.set("explosions", explosions);
        plugin.getDataManager().saveConfig();
    }

    public void addBuilder(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> players = new ArrayList<>((ArrayList<OfflinePlayer>) cfg.getList("buildTrusted"));
        players.add(player);
        cfg.set("buildTrusted", players);
        plugin.getDataManager().saveConfig();
    }

    public void addContainer(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> players = new ArrayList<>((ArrayList<OfflinePlayer>) cfg.getList("containertrusted"));
        players.add(player);
        cfg.set("containerTrusted", players);
        plugin.getDataManager().saveConfig();
    }

    public void addAccess(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> players = new ArrayList<>((ArrayList<OfflinePlayer>) cfg.getList("accessTrusted"));
        players.add(player);
        cfg.set("accessTrusted", players);
        plugin.getDataManager().saveConfig();
    }

    public void removeTrust(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<OfflinePlayer> at = new ArrayList<>((ArrayList<OfflinePlayer>) cfg.getList("accessTrusted"));
        at.remove(player);
        cfg.set("accessTrusted", at);
        ArrayList<OfflinePlayer> ct = new ArrayList<>((ArrayList<OfflinePlayer>) cfg.getList("containerTrusted"));
        ct.remove(player);
        cfg.set("containerTrusted", ct);
        ArrayList<OfflinePlayer> t = new ArrayList<>((ArrayList<OfflinePlayer>) cfg.getList("buildTrusted"));
        t.remove(player);
        cfg.set("buildTrusted", t);
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

    public ArrayList<String> getClaims(Player player) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims");
        ArrayList<String> claimIDs = new ArrayList<>();
        for(String key : cfg.getKeys(false)) {
            if(getOwner(key).equals(player)) {
                claimIDs.add(key);
            }
        }
        return claimIDs;
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

    public String getCreated(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return cfg.getString("created");
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

    public boolean getHasPermission(OfflinePlayer p, Location loc, int requiredLevel) { // 1 = owner/all, 2 = build, 3 = container, 4 = access, 5 = none
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

    public boolean getHasPermission(OfflinePlayer p, String claimID, int requiredLevel) { // 1 = owner/all, 2 = build, 3 = container, 4 = access, 5 = none
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

    public boolean getExplosions(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return cfg.getBoolean("explosions");
    }

    public ArrayList<Subdivision> getSubdivisions(String claimID) {
        ArrayList<Subdivision> list = new ArrayList<>();
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        list.addAll((ArrayList<Subdivision>) cfg.getList("subdivisions"));
        return list;
    }

    //print
    public String toString(Player player, String claimID) {
        StringBuilder result = new StringBuilder();
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        boolean owner = getOwner(claimID).equals(player);
        BoundingBox box = (BoundingBox) cfg.get("boundingBox");
        int x1 = (int) box.getMaxX();
        int z1 = (int) box.getMaxY();
        int x2 = (int) box.getMinX();
        int z2 = (int) box.getMinY();
        int size = (x1 - z1) * (x2 - z2);

        result.append(TextUtil.convertColor("&6Claim at (" + x1 + ", " + z1 + ") -> (" + x2 + ", " + z2 + ")\n"));
        result.append(TextUtil.convertColor("&7Owner: &f" + getOwner(claimID).getName() + "\nArea: " + size + "m &f| &7Created: " + getCreated(claimID)));

        if(!owner) return result.toString();

        //trusted, container trusted, access trusted
        result.append(TextUtil.convertColor("\n&9Trusted: "));
        for(OfflinePlayer p : getBuilders(claimID)) result.append(p.getName()).append(" ");
        result.append(TextUtil.convertColor("\n&aContainer-trusted: "));
        for(OfflinePlayer p : getBuilders(claimID)) result.append(p.getName()).append(" ");
        result.append(TextUtil.convertColor("\n&eAccess-trusted: "));
        for(OfflinePlayer p : getBuilders(claimID)) result.append(p.getName()).append(" ");
        return result.toString();
    }

}
