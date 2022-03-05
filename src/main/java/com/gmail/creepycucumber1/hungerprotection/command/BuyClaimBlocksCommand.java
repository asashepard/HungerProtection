package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimBlocksGUI;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyClaimBlocksCommand extends CommandBase {
    public BuyClaimBlocksCommand(HungerProtection plugin) {
        super(plugin, "buyclaimblocks", "Buy claim blocks", "");
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
            player.sendMessage(TextUtil.convertColor("&cYou need $" + (blocks * 2 - plugin.getVault().getBalance(player)) + " more to purchase " + blocks + " blocks."));
            return true;
        }
        else {
            plugin.getVault().withdrawPlayer(player, blocks * 2);
            plugin.getPlayerManager().addClaimBlocks(player, blocks);
            player.sendMessage(TextUtil.convertColor("&6You have purchased &e" + blocks + " &6claim block" + (blocks == 1 ? "s" : "") + " for &e$" + (blocks * 2) + "."));
        }

        return true;
    }
}
