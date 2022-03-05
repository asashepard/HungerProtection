package com.gmail.creepycucumber1.hungerprotection.util;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Util {

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
