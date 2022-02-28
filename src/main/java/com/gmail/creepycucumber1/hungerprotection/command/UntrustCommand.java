package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class UntrustCommand extends CommandBase {
    public UntrustCommand(HungerProtection plugin) {
        super(plugin, "untrust", "Strip another player's permissions in your claim(s)", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
