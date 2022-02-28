package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class ClaimTopCommand extends CommandBase {
    public ClaimTopCommand(HungerProtection plugin) {
        super(plugin, "claimtop", "Get a leaderboard of the top claimers on the server", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
