package com.gmail.creepycucumber1.hungerprotection.util;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Util {

    private static HungerProtection plugin;

    public static boolean getHasPermission(Player p, Location loc, int requiredLevel) { // 1 = owner/all, 2 = build, 3 = container, 4 = access, 5 = none
        String claimID = plugin.getClaimManager().getClaim(loc);

        if(claimID.equalsIgnoreCase("none")) return true; //no claim at location
        if(p.isOp()) return true; //player is operator

        int level = Integer.MAX_VALUE;

        if(plugin.getClaimManager().getOwner(claimID).equals(p)) level = 1; //is claim owner
        else if(plugin.getClaimManager().getBuilders(claimID).contains(p)) level = 2; //has build permission
        else if(plugin.getClaimManager().getContainer(claimID).contains(p)) level = 3; //has container permission
        else if(plugin.getClaimManager().getAccess(claimID).contains(p)) level = 4; //has access permission

        if(level > requiredLevel)
            p.sendMessage(TextUtil.convertColor("&cThat block is claimed by " +
                    (plugin.getClaimManager().getIsAdmin(claimID) ? "an administrator" : plugin.getClaimManager().getOwner(claimID).getName())));
        return level <= requiredLevel;
    }

    public static Location getHighest(Player p, int x, int z) {
        World world = p.getWorld();
        int playerY = p.getLocation().getBlockY();
        int y = playerY;
        int highestY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

        if(highestY < playerY) return new Location(world, x, highestY, z);

        while(!world.getBlockAt(x, y, z).getType().equals(Material.AIR))
            y++;

        return new Location(world, x, y, z);
    }

}
