package com.gmail.creepycucumber1.hungerprotection.util;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Util {

    public static Location getHighest(Player p, int x, int z) {
        World world = p.getWorld();
        int playerY = p.getLocation().getBlockY();
        int highestY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);

        //above ground
        if(highestY < playerY) return new Location(world, x, highestY, z);

        //underground
        int y = playerY;
        if(world.getBlockAt(x, y, z).isCollidable()) {
            while(y < world.getMaxHeight() && world.getBlockAt(x, y + 1, z).getType().isCollidable() || world.getBlockAt(x, y, z).isLiquid())
                y++;
        }
        else {
            while(y > world.getMinHeight() && !world.getBlockAt(x, y, z).getType().isCollidable() && !world.getBlockAt(x, y, z).isLiquid())
                y--;
        }

        return new Location(world, x, y, z);
    }

}
