package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class AccessTrustCommand extends CommandBase {
    public AccessTrustCommand(HungerProtection plugin) {
        super(plugin, "accesstrust", "Trust another player to use buttons etc. in your claim(s)", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
