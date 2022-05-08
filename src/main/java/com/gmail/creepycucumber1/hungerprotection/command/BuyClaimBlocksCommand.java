package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimBlocksGUI;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyClaimBlocksCommand extends CommandBase {
    public BuyClaimBlocksCommand(HungerProtection plugin) {
        super(plugin, "buyclaimblocks", "Buy claim blocks", "", "bcb");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }

        if(args.length == 0) {
            plugin.getGuiManager().openGUI(player, new ClaimBlocksGUI(plugin, player));
            return true;
        }
        int blocks = Integer.parseInt(args[0]);
        if(plugin.getVault().getBalance(player) < blocks * 2) {
            player.sendMessage(TextUtil.convertColor("&7You need &f$" + (blocks * 2 - plugin.getVault().getBalance(player)) + " &7more to purchase &f" + blocks + " &7blocks."));
            return true;
        }
        else {
            plugin.getVault().withdrawPlayer(player, blocks * 2);
            plugin.getPlayerManager().addClaimBlocks(player, blocks);
            player.sendMessage(TextUtil.convertColor("&aYou have purchased &f" + blocks + " &aclaim block" + (blocks == 1 ? "s" : "") + " for &f$" + (blocks * 2) + "&a."));
        }

        return true;
    }
}
