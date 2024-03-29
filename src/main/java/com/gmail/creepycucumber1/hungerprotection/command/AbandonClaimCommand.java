package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
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
            return true;
        }

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        String claimID = plugin.cm().getClaim(player.getLocation());

        if(claimID.equalsIgnoreCase("none")) {
            player.sendMessage(TextUtil.convertColor("&7Stand within a claim to abandon it."));
            return true;
        }
        if(!plugin.cm().getOwner(claimID).equals(player) && !(plugin.cm().getIsAdmin(claimID) && player.isOp())) {
            player.sendMessage(TextUtil.convertColor("&7Stand within a claim you own to abandon it."));
            return true;
        }

        for(Subdivision subdivision : plugin.cm().getSubdivisions(claimID)) {
            if(subdivision.getBoundingBox().contains(player.getLocation().toVector())) {
                plugin.cm().removeSubdivision(subdivision, claimID);
                player.sendMessage(TextUtil.convertColor("&aSubdivision successfully removed."));
                return true;
            }
        }

        BoundingBox box = plugin.cm().getBoundingBox(claimID);
        if(!player.isOp()) {
            int size = (int) box.getWidthX() * (int) box.getWidthZ();
            plugin.getPlayerManager().addClaimBlocks(player, size);
        }
        plugin.cm().removeClaim(claimID);
        player.sendMessage(TextUtil.convertColor("&aClaim successfully removed. You now have &f" +
                plugin.getPlayerManager().getClaimBlocks(player) + " &aclaim blocks remaining."));

        return true;
    }
}
