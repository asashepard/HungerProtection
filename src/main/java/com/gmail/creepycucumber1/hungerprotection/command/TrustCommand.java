package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class TrustCommand extends CommandBase {
    public TrustCommand(HungerProtection plugin) {
        super(plugin, "trust", "Trust another player to build in your claim(s)", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
