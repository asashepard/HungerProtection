package com.gmail.creepycucumber1.hungerprotection.runnable;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Teleport {

    private HungerProtection plugin;

    public Teleport(HungerProtection plugin) {
        this.plugin = plugin;
    }

    public void teleport(Player player, World world, double x, double y, double z) {

        ArrayList<Double> healthValues = new ArrayList<>();

        Location startPos = player.getLocation();
        healthValues.add(player.getHealth());
        Location telePos = new Location(world, x, y, z);

        player.sendMessage(TextUtil.convertColor("&aTeleportation will begin in 5 seconds..."));

        for(int i = 10; i < 100; i += 10) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    healthValues.add(player.getHealth());
                }
            }, i);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {

                Location endPos = player.getLocation();
                boolean tookDamage = false;
                for(int i = 0; i < healthValues.size() - 1; i++)
                    if(healthValues.get(i + 1) < healthValues.get(i)) {
                        tookDamage = true;
                        break;
                    }

                if(endPos.distance(startPos) > 2 || tookDamage)
                    player.sendMessage(TextUtil.convertColor("&cYou moved or took damage! Teleportation has been canceled."));
                else {
                    plugin.getEssentials().getUser(player.getUniqueId()).setLastLocation(endPos);
                    player.teleport(telePos);
                }

            }
        }, 100);

    }

}
