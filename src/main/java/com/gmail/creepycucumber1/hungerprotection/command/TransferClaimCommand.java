package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class TransferClaimCommand extends CommandBase {
    public TransferClaimCommand(HungerProtection plugin) {
        super(plugin, "transferclaim", "Transfer ownership of a claim to another player", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
