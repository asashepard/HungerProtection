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
                        int amount = 50;
                        if(plugin.getEssentials().getUser(player).isAfk())
                            amount = 10;
                        plugin.getPlayerManager().addAccruedBlocks(player, amount);
                        plugin.getPlayerManager().addClaimBlocks(player, amount);
                    }
                });
            }

        }, 0, 36000); //10 minutes
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
}
