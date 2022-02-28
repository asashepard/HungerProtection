package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class ClaimCommand extends CommandBase {
    public ClaimCommand(HungerProtection plugin) {
        super(plugin, "claim", "Instantly claim a 5x5 plot of land", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
