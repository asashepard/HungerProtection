package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.claim.PlayerManager;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TrustCommand extends CommandBase {
    public TrustCommand(HungerProtection plugin) {
        super(plugin, "trust", "Trust another player to build in your claim(s)", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        String claimID = plugin.cm().getClaim(player.getLocation());

        if(claimID.equalsIgnoreCase("none")) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim to manage permissions."));
            return true;
        }
        if(!plugin.cm().getOwner(claimID).equals(player)) {
            player.sendMessage(TextUtil.convertColor("&cStand within a claim that you own to manage permissions."));
            return true;
        }
        if(args.length == 0) {
            player.sendMessage(TextUtil.convertColor("&cPlease specify a player to manage permissions."));
            return true;
        }

        PlayerManager pm = plugin.getPlayerManager();
        ArrayList<String> names = new ArrayList<>();
        for(OfflinePlayer p : pm.getPlayers()) names.add(p.getName());
        if(!names.contains(args[0])) {
            player.sendMessage(TextUtil.convertColor("&cThat player hasn't logged on before."));
            return true;
        }
        OfflinePlayer toAdd = Bukkit.getOfflinePlayer(args[0]);

        if(args.length == 2 && args[1].equalsIgnoreCase("all")) {
            int count = 0;
            for(String cid : plugin.cm().getClaims(player)) {
                if(!plugin.cm().getHasPermission(toAdd, cid, 2))
                    plugin.cm().addAccess(toAdd, cid);
                count++;
            }
            player.sendMessage(TextUtil.convertColor("&aSuccessfully trusted " + args[0] + " in all " + count + " claims you own."));
            return true;
        }
        else if(plugin.cm().getHasPermission(toAdd, claimID, 2)) {
            player.sendMessage(TextUtil.convertColor("&cThat player is already trusted in this claim."));
            return true;
        }
        plugin.cm().addAccess(toAdd, claimID);
        player.sendMessage(TextUtil.convertColor("&aSuccessfully trusted " + args[0] + " in this claim."));

        return false;
    }
}
