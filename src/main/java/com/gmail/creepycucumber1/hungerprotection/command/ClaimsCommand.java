package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.items.ClaimsGUI;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimsCommand extends CommandBase {
    public ClaimsCommand(HungerProtection plugin) {
        super(plugin, "claims", "List all of your claims", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }

        plugin.getGuiManager().openGUI(player, new ClaimsGUI(plugin, player));
        return true;
    }
}
