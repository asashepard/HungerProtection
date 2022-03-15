package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimsGUI;
import com.gmail.creepycucumber1.hungerprotection.items.PlayersGUI;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TransferClaimCommand extends CommandBase {
    public TransferClaimCommand(HungerProtection plugin) {
        super(plugin, "transferclaim", "Transfer ownership of a claim to another player", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        String claimID = plugin.cm().getClaim(player.getLocation());

        if(claimID.equalsIgnoreCase("none")) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim to transfer it."));
            return true;
        }
        if(!plugin.cm().getOwner(claimID).equals(player)) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim that you own to transfer it. " +
                    "Imagine the chaos that might ensue if you could transfer any claim......"));
            return true;
        }
        if(args.length == 0) {
            plugin.getGuiManager().openGUI(player, new PlayersGUI(plugin, player, claimID, "transferclaim"));
            return true;
        }

        PlayerManager pm = plugin.getPlayerManager();

        if(args.length == 2) {
            claimID = args[0];
            if(!plugin.cm().getClaims(player).contains(claimID)) return true;
            ArrayList<String> names = new ArrayList<>();
            for(OfflinePlayer p : pm.getPlayers()) names.add(p.getName());
            if(!names.contains(args[0])) {
                player.sendMessage(TextUtil.convertColor("&cPlayer " + args[1] + " hasn't logged on before."));
                return true;
            }
            OfflinePlayer toTransfer = Bukkit.getOfflinePlayer(args[1]);
            plugin.cm().setOwner(toTransfer, claimID);
        }

        ArrayList<String> names = new ArrayList<>();
        for(OfflinePlayer p : pm.getPlayers()) names.add(p.getName());
        if(!names.contains(args[0])) {
            player.sendMessage(TextUtil.convertColor("&cPlayer " + args[0] + " hasn't logged on before."));
            return true;
        }
        OfflinePlayer toTransfer = Bukkit.getOfflinePlayer(args[0]);

        if(args.length == 2 && args[1].equalsIgnoreCase("all")) {
            int count = 0;
            for(String cid : plugin.cm().getClaims(player)) {
                plugin.cm().setOwner(toTransfer, cid);
                count++;
            }
            player.sendMessage(TextUtil.convertColor("&aSuccessfully transferred all " + count + " claims you owned to " + args[0] + "."));
            return true;
        }
        plugin.cm().setOwner(toTransfer, claimID);
        player.sendMessage(TextUtil.convertColor("&aSuccessfully transferred this claim to " + args[0] + "."));

        return false;
    }
}
