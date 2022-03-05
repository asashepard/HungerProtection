package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class AbandonClaimCommand extends CommandBase {
    public AbandonClaimCommand(HungerProtection plugin) {
        super(plugin, "abandonclaim", "Abandon a claim", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(args.length == 1 && sender.isOp()) {
            String cid = args[0];
            plugin.cm().removeClaim(cid);
            sender.sendMessage("Successfully abandoned claim " + cid);
        }

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        String claimID = plugin.cm().getClaim(player.getLocation());

        if(claimID.equalsIgnoreCase("none")) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim to abandon it."));
            return true;
        }
        if(!plugin.cm().getOwner(claimID).equals(player)) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim that you own to abandon it."));
            return true;
        }

        BoundingBox box = plugin.cm().getBoundingBox(claimID);
        int size = (int) Math.abs(box.getMaxX() - box.getMinX()) * (int) Math.abs(box.getMaxZ() - box.getMinZ());
        plugin.getPlayerManager().addClaimBlocks(player, size);
        plugin.cm().removeClaim(claimID);
        player.sendMessage(TextUtil.convertColor("&6Claim successfully removed. You now have " +
                plugin.getPlayerManager().getClaimBlocks(player) + " claim blocks remaining."));

        return false;
    }
}
