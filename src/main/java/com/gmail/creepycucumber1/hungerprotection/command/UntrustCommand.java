package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.claim.Subdivision;
import com.gmail.creepycucumber1.hungerprotection.items.PlayersGUI;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class UntrustCommand extends CommandBase {
    public UntrustCommand(HungerProtection plugin) {
        super(plugin, "untrust", "Strip another player's permissions in your claim(s)", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        String claimID = plugin.cm().getClaim(player.getLocation());

        if(claimID.equalsIgnoreCase("none")) {
            player.sendMessage(TextUtil.convertColor("&7Stand within a claim to manage permissions."));
            return true;
        }
        if(!plugin.cm().getOwner(claimID).equals(player) && !(plugin.cm().getIsAdmin(claimID) && player.isOp())) {
            player.sendMessage(TextUtil.convertColor("&7Stand within a claim that you own to manage permissions."));
            return true;
        }
        if(args.length == 0) {
            //subdivisions
            boolean privatized = false;
            for(Subdivision subdivision : plugin.cm().getSubdivisions(claimID)) {
                if(subdivision.getBoundingBox().contains(player.getLocation().toVector())) {
                    plugin.cm().removeSubdivision(subdivision, claimID);
                    subdivision.setPrivate(true);
                    plugin.cm().addSubdivision(subdivision, claimID);
                    privatized = true;
                }
            }
            if(privatized) {
                player.sendMessage(TextUtil.convertColor("&aAll subdivisions that overlap your current location are now set to private."));
                return true;
            }
            plugin.getGuiManager().openGUI(player, new PlayersGUI(plugin, player, claimID, "untrust"));
            return true;
        }

        if(args[0].equalsIgnoreCase("public") || args[0].equalsIgnoreCase("p")) {
            plugin.cm().setPublic(5, claimID);
            player.sendMessage(TextUtil.convertColor("&aSuccessfully un-trusted the public in this claim. " +
                    "All individual permissions still remain."));
            return true;
        }
        if(args[0].equalsIgnoreCase("clan")) {
            plugin.cm().setClan("none", claimID);
            player.sendMessage(TextUtil.convertColor("&aSuccessfully un-trusted your clan in this claim. " +
                    "All individual permissions still remain."));
            return true;
        }

        PlayerManager pm = plugin.getPlayerManager();
        ArrayList<String> names = new ArrayList<>();
        for(OfflinePlayer p : pm.getPlayers()) names.add(p.getName());
        if(!names.contains(args[0])) {
            player.sendMessage(TextUtil.convertColor("&cThat player hasn't logged on before."));
            return true;
        }
        OfflinePlayer toRemove = Bukkit.getOfflinePlayer(args[0]);

        if(args.length == 2 && args[1].equalsIgnoreCase("all")) {
            int count = 0;
            for(String cid : plugin.cm().getClaims(player)) {
                plugin.cm().removeTrust(toRemove, cid);
                count++;
            }
            player.sendMessage(TextUtil.convertColor("&aSuccessfully untrusted " + args[0] + " in all " + count + " claims you own."));
            return true;
        }
        else if(!plugin.cm().getHasPermission(toRemove, claimID, 4)) {
            player.sendMessage(TextUtil.convertColor("&cThat player is already untrusted in this claim."));
            return true;
        }
        plugin.cm().removeTrust(toRemove, claimID);
        player.sendMessage(TextUtil.convertColor("&aSuccessfully untrusted " + args[0] + " in this claim."));

        return false;
    }
}
