package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ClaimExplosionsCommand extends CommandBase {
    public ClaimExplosionsCommand(HungerProtection plugin) {
        super(plugin, "claimexplosions", "Toggle explosions within a claim", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        String claimID = plugin.cm().getClaim(player.getLocation());

        if(claimID.equalsIgnoreCase("none")) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim to manage explosions."));
            return true;
        }
        if(!plugin.cm().getOwner(claimID).equals(player)) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim that you own to manage explosions."));
            return true;
        }

        for(Subdivision subdivision : plugin.cm().getSubdivisions(claimID)) {
            if(subdivision.getBoundingBox().contains(player.getLocation().toVector())) {
                subdivision.setExplosions(!subdivision.getIsExplosions());
                player.sendMessage(TextUtil.convertColor("&aSuccessfully toggled explosions in this subdivision! Explosions are now " +
                        (subdivision.getIsExplosions() ? "&2enabled&a." : "&4disabled&a.")));
                return true;
            }
        }
        plugin.cm().setExplosions(claimID, !plugin.cm().getExplosions(claimID));
        player.sendMessage(TextUtil.convertColor("&aSuccessfully toggled explosions in this claim! Explosions are now " +
                (plugin.cm().getExplosions(claimID) ? "&2enabled&a." : "&4disabled&a.")));
        return true;

    }
}
