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
        int highestY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

        //above ground
        if(highestY < playerY) return new Location(world, x, highestY, z);

        //underground
        int y = playerY;
        if(world.getBlockAt(x, y, z).isCollidable()) {
            while(world.getBlockAt(x, y + 1, z).getType().isCollidable() || world.getBlockAt(x, y, z).isLiquid())
                y++;
        }
        else {
            while(!world.getBlockAt(x, y, z).getType().isCollidable() && !world.getBlockAt(x, y, z).isLiquid())
                y--;
        }

        return new Location(world, x, y, z);
    }

}
