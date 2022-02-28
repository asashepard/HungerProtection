package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import org.bukkit.command.CommandSender;

public class ContainerTrustCommand extends CommandBase {
    public ContainerTrustCommand(HungerProtection plugin) {
        super(plugin, "containertrust", "Trust another player to open containers in your claim(s)", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        //logic

        return false;
    }
}
