package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class ClaimsCommand extends CommandBase {
    public ClaimsCommand(HungerProtection plugin) {
        super(plugin, "claims", "List all of your claims", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
