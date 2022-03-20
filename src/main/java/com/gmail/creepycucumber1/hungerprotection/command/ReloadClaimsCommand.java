package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.command.CommandSender;

public class ReloadClaimsCommand extends CommandBase {
    public ReloadClaimsCommand(HungerProtection plugin) {
        super(plugin, "reloadclaims", "Refresh and sync claims with the data file", "", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!sender.isOp()) {
            sender.sendMessage(TextUtil.convertColor("&cYou don't have access to this command."));
            return true;
        }

        plugin.getDataManager().reloadConfig();

        return true;
    }
}
