package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimInspectionTool;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimTool;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.ArrayList;

public class ClaimToolsCommand extends CommandBase {
    public ClaimToolsCommand(HungerProtection plugin) {
        super(plugin, "claimtools", "Get tools to manage claims", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        if(Math.abs(plugin.getPlayerManager().getLastTools(player) - Instant.now().toEpochMilli()) < 300000) {
            player.sendMessage(TextUtil.convertColor("&cEasy there, there's a five minute cooldown for those!"));
            return true;
        }

        ArrayList<Integer> emptySlots = new ArrayList<>();
        for(int i = 0; i <= 35; i++) {
            if(player.getInventory().getItem(i) == null)
                emptySlots.add(i);
        }
        if(emptySlots.size() < 2) {
            player.sendMessage(TextUtil.convertColor("&cHey, no need to flex! You have too few slots in your inventory!"));
            return true;
        }

        player.getInventory().setItem(emptySlots.get(0), ClaimTool.claimTool);
        player.getInventory().setItem(emptySlots.get(1), ClaimInspectionTool.claimInspectionTool);
        plugin.getPlayerManager().setLastToolsToNow(player);

        return true;
    }
}
