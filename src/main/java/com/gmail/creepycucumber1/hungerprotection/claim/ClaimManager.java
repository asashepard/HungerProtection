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
        BoundingBox box = new BoundingBox(x1, world.getMinHeight(), z1, x2, world.getMaxHeight(), z2);
        box.expand(0, 0, 0, 1, 0, 1);
        if(overlaps(box, world, owner, "")) return false;

        //too small
        if(box.getWidthX() < 4 || box.getWidthZ() < 4) {
            owner.sendMessage(TextUtil.convertColor("&cA claim must be at least 5 blocks wide in each direction."));
            return false;
        }

        //price management
        int size = Math.abs(x2 - x1) * Math.abs(z2 - z1);
        if(!owner.isOp() && plugin.getPlayerManager().getClaimBlocks(owner) < size) {
            owner.sendMessage(TextUtil.convertColor("&cYou need " + (size - plugin.getPlayerManager().getClaimBlocks(owner)) + " more claim blocks to claim this area."));
            TextUtil.sendClickableCommand(owner, TextUtil.convertColor("&aClick here to buy claim blocks"), "/buyclaimblocks", "Open the claim blocks menu");
            return false;
        }

        //--create claim--

        //generate unique identifier
        String claimID = UUID.randomUUID().toString();
        Bukkit.getLogger().info("New claim created: " + claimID);
        //created
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String created = dateFormat.format(date);
        //buildTrusted, containerTrusted, accessTrusted
        ArrayList<String> buildTrusted = new ArrayList<>();
        ArrayList<String> containerTrusted = new ArrayList<>();
        ArrayList<String> accessTrusted = new ArrayList<>();
        //subdivisions
        ArrayList<Subdivision> subdivisions = new ArrayList<>();

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
        map.put("explosions", false);

        if(!owner.isOp()) plugin.getPlayerManager().removeClaimBlocks(owner, size);
        plugin.getDataManager().getConfig().createSection("claims." + claimID, map);
        plugin.getDataManager().saveConfig();

        owner.sendMessage(TextUtil.convertColor("&aYou have successfully made a claim!"));
        if(!owner.isOp()) owner.sendMessage(TextUtil.convertColor("&7You have " + plugin.getPlayerManager().getClaimBlocks(owner) + " claim blocks remaining."));
        PacketManager.highlightClaim(owner, claimID, false);

        return true;
    }

    public void removeClaim(String claimID) {
        boolean exists = false;
        for(String cid : plugin.getDataManager().getConfig().getConfigurationSection("claims").getKeys(false)) {
            if(cid.equalsIgnoreCase(claimID)) {
                exists = true;
                break;
            }
        }
        if(!exists) return;

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
                int z1 = (int) otherBox.getMinZ();
                int x2 = (int) otherBox.getMaxX();
                int z2 = (int) otherBox.getMaxZ();
                player.sendMessage(TextUtil.convertColor("&cThis selection overlaps an existing claim at " +
                        "(" + x1 + ", " + z1 + ") -> (" + x2 + ", " + z2 + ")."));
                PacketManager.highlightClaim(player, key, true);
                return true;
            }
        }
        return false;
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

        int fX1;
        int fZ1;

        int xMax = (int) box.getMaxX();
        int zMax = (int) box.getMaxZ();
        int xMin = (int) box.getMinX();
        int zMin = (int) box.getMinZ();
        //set fX1 and fX2 to the corner opposite the one being resized
        if(Math.abs(xMax - x1) < Math.abs(xMin - x1))
            fX1 = xMin;
        else
            fX1 = xMax;
        if(Math.abs(zMax - z1) < Math.abs(zMin - z1))
            fZ1 = zMin;
        else
            fZ1 = zMax;

        BoundingBox newBox = new BoundingBox(fX1, world.getMinHeight(), fZ1, x2, world.getMaxHeight(), z2);
        newBox = newBox.expand(0, 0, 0, (fX1 == xMax ? 0 : 1), 0, (fZ1 == zMax ? 0 : 1));

        if(!overlaps(newBox, world, player, claimID) && containsAllSubdivisions(newBox, (ArrayList<Subdivision>) cfg.getList("subdivisions")))
            if(newBox.getWidthX() >= 4 && newBox.getWidthZ() >= 4)
                cfg.set("boundingBox", newBox);
            else
                player.sendMessage(TextUtil.convertColor("&7Claims must remain at least 5 blocks wide in either direction."));

        plugin.getDataManager().saveConfig();
    }

    public void setExplosions(String claimID, boolean explosions) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        cfg.set("explosions", explosions);
        plugin.getDataManager().saveConfig();
    }

    public void addBuilder(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<String> list = new ArrayList<>(cfg.getStringList("buildTrusted"));
        list.add(player.getUniqueId().toString());
        cfg.set("buildTrusted", list);
        plugin.getDataManager().saveConfig();
    }

    public void addContainer(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<String> list = new ArrayList<>(cfg.getStringList("containerTrusted"));
        list.add(player.getUniqueId().toString());
        cfg.set("containerTrusted", list);
        plugin.getDataManager().saveConfig();
    }

    public void addAccess(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<String> list = new ArrayList<>(cfg.getStringList("accessTrusted"));
        list.add(player.getUniqueId().toString());
        cfg.set("accessTrusted", list);
        plugin.getDataManager().saveConfig();
    }

    public void removeTrust(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<String> at = new ArrayList<>(cfg.getStringList("accessTrusted"));
        at.add(player.getUniqueId().toString());
        cfg.set("accessTrusted", at);
        ArrayList<String> ct = new ArrayList<>(cfg.getStringList("containerTrusted"));
        ct.add(player.getUniqueId().toString());
        cfg.set("containerTrusted", ct);
        ArrayList<String> t = new ArrayList<>(cfg.getStringList("buildTrusted"));
        t.add(player.getUniqueId().toString());
        cfg.set("buildTrusted", t);
        plugin.getDataManager().saveConfig();
    }

    public void setOwner(OfflinePlayer player, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        cfg.set("owner", player.getUniqueId().toString());
        plugin.getDataManager().saveConfig();
    }

    public void addSubdivision(Subdivision subdivision, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<Subdivision> subdivisions = new ArrayList<>((ArrayList<Subdivision>) cfg.get("subdivisions"));
        subdivisions.add(subdivision);
        cfg.set("subdivisions", subdivisions);
        plugin.getDataManager().saveConfig();
    }

    public void removeSubdivision(Subdivision subdivision, String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        ArrayList<Subdivision> subdivisions = new ArrayList<>((ArrayList<Subdivision>) cfg.get("subdivisions"));
        subdivisions.remove(subdivision);
        cfg.set("subdivisions", subdivisions);
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

    public ArrayList<String> getClaims(OfflinePlayer player) {
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
        if(claimID.equalsIgnoreCase("none")) return false;
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return cfg.getBoolean("isAdmin");
    }

    public BoundingBox getBoundingBox(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return (BoundingBox) cfg.get("boundingBox");
    }

    public BoundingBox getVisualBox(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        BoundingBox box = (BoundingBox) cfg.get("boundingBox");
        return new BoundingBox(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX() - 1, box.getMaxY(), box.getMaxZ() - 1);
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

    public boolean getHasPermission(OfflinePlayer p, String claimID, int requiredLevel) { // 1 = owner/all, 2 = build, 3 = container, 4 = access, 5 = none
        if(p.isOp()) return true; //player is operator
        if(claimID.equalsIgnoreCase("none")) return true; //no claim in the location

        int level = 5;

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
        if(claimID.equalsIgnoreCase("none")) return true; //no claim in the location
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        return cfg.getBoolean("explosions");
    }

    public boolean getExplosions(Location loc) {
        String claimID = getClaim(loc);
        if(claimID.equalsIgnoreCase("none")) return true;
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        boolean explosions = getExplosions(claimID);
        try {
            for(Subdivision subdivision : (ArrayList<Subdivision>) cfg.getList("subdivisions")) {
                if(subdivision.getBoundingBox().contains(loc.toVector()))
                    explosions = subdivision.getIsExplosions();
            }
        } catch (NullPointerException ignored) {}
        return explosions;
    }

    public ArrayList<Subdivision> getSubdivisions(String claimID) {
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        try {
            cfg.getList("subdivisions");
        } catch (NullPointerException e) {
            return new ArrayList<>();
        }
        ArrayList<Subdivision> list = new ArrayList<>((ArrayList<Subdivision>) cfg.getList("subdivisions"));
        return list;
    }

    public boolean isPrivatized(Location loc) {
        String claimID = getClaim(loc);
        if(claimID.equalsIgnoreCase("none")) return false;
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        boolean prvt = false;
        for(Subdivision subdivision : (ArrayList<Subdivision>) cfg.getList("subdivisions")) {
            if(subdivision.getBoundingBox().contains(loc.toVector()) && subdivision.getIsPrivate())
                prvt = true;
        }
        return prvt;
    }

    //print
    public String toString(Player player, String claimID) {
        StringBuilder result = new StringBuilder();
        ConfigurationSection cfg = plugin.getDataManager().getConfig().getConfigurationSection("claims." + claimID);
        boolean owner = getOwner(claimID).equals(player);
        BoundingBox box = (BoundingBox) cfg.get("boundingBox");
        int x1 = (int) box.getMaxX();
        int z1 = (int) box.getMaxZ();
        int x2 = (int) box.getMinX();
        int z2 = (int) box.getMinZ();
        int size = (int) box.getWidthX() * (int) box.getWidthZ();

        result.append(TextUtil.convertColor("&2&lClaim at (" + x1 + ", " + z1 + ") -> (" + x2 + ", " + z2 + ")\n"));
        result.append(TextUtil.convertColor("&7Owner: &f" + getOwner(claimID).getName() + "\nArea: " + size + "m &f| &7Created: " + getCreated(claimID)));

        if(!owner) return result.toString();

        //trusted, container trusted, access trusted
        result.append(TextUtil.convertColor("\n&9Trusted: "));
        for(OfflinePlayer p : getBuilders(claimID)) result.append(p.getName()).append(" ");
        result.append(TextUtil.convertColor("\n&dContainer-trusted: "));
        for(OfflinePlayer p : getBuilders(claimID)) result.append(p.getName()).append(" ");
        result.append(TextUtil.convertColor("\n&eAccess-trusted: "));
        for(OfflinePlayer p : getBuilders(claimID)) result.append(p.getName()).append(" ");
        return result.toString();
    }

}
