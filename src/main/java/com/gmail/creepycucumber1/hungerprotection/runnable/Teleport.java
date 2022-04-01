package com.gmail.creepycucumber1.hungerprotection.runnable;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Teleport {

    private HungerProtection plugin;

    public Teleport(HungerProtection plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player, World world, double x, double y, double z) {

        Location startPos = player.getLocation();
        double startHealth = player.getHealth();
        Location telePos = new Location(world, x, y, z);

        player.sendMessage(TextUtil.convertColor("&aTeleportation will begin in 5 seconds..."));

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {

                Location endPos = player.getLocation();
                double endHealth = player.getHealth();
                if(endPos.distance(startPos) > 2 || startHealth > endHealth)
                    player.sendMessage(TextUtil.convertColor("&cYou moved or took damage! Teleportation has been canceled."));
                else
                    player.teleport(telePos);

            }
        }, 100);

    }

}
