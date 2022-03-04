package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class AbandonClaimCommand extends CommandBase {
    public AbandonClaimCommand(HungerProtection plugin) {
        super(plugin, "abandonclaim", "Abandon a claim", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
