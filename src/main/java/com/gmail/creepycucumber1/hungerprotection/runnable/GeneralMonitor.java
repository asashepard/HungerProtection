package com.gmail.creepycucumber1.hungerprotection.runnable;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.event.PacketManager;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimTool;
import org.bukkit.Bukkit;

public class GeneralMonitor {

    private HungerProtection plugin;
    private static final int MAX_ACCRUE = 36000;

    public GeneralMonitor(HungerProtection plugin) {
        this.plugin = plugin;
    }

    public void monitorPlayers() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(plugin.getPlayerManager().getAccruedBlocks(player) < MAX_ACCRUE) {
                        int amount = 5;
                        if(plugin.getEssentials().getUser(player).isAfk())
                            amount = 1;
                        plugin.getPlayerManager().addAccruedBlocks(player, amount);
                        plugin.getPlayerManager().addClaimBlocks(player, amount);
                    }
                });
            }

        }, 0, 3600); //1 minute
    }

    public void monitorPlayerHand() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(player.getInventory().getItemInMainHand().equals(ClaimTool.claimTool)) {
                        String claimID = plugin.cm().getClaim(player.getLocation());
                        if(!claimID.equalsIgnoreCase("none") && plugin.cm().getOwner(claimID).getPlayer().equals(player))
                            PacketManager.highlightClaim(player, claimID, false);
                    }
                    else {
                        if(plugin.getPlayerManager().isClaiming(player))
                            plugin.getPlayerManager().resetCurrentClaimingData(player);
                    }
                });
            }

        }, 0, 40); //2 seconds
    }

    public void monitorOften() {

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            public void run() { //extra layer of end island claim prevention
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(player.getWorld().equals(Bukkit.getWorld("world_the_end"))) {
                        if(Math.abs(player.getLocation().getX()) < 155 ||
                                Math.abs(player.getLocation().getZ()) < 155) {
                            plugin.getPlayerManager().resetCurrentClaimingData(player);
                        }
                    }
                });
            }

        }, 0, 5); //0.5 seconds
    }
}
