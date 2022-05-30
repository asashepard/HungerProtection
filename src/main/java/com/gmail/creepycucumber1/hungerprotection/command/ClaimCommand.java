package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.event.PacketManager;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand extends CommandBase {
    public ClaimCommand(HungerProtection plugin) {
        super(plugin, "claim", "Instantly claim a 5x5 plot of land", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }

        if(args.length > 0 && !args[0].equalsIgnoreCase("subdivisions")) {
            player.sendMessage(TextUtil.convertColor("&2&lClaim Commands: &r\n" +
                    " &8- &r&o/claim &r&7| create a new 5x5 claim&r\n" +
                    " &8- &r&o/claimtool &r&7| get tools to manage claims&r\n" +
                    " &8- &r&o/claims &r&7| list your claims&r\n" +
                    " &8- &r&o/claimtop &r&7| get server leaderboard&r\n" +
                    " &8- &r&o/buyclaimblocks [#] &r&7| buy claim blocks&r\n" +
                    " &8- &r&o/at [player] &r&7| accesstrust&r\n" +
                    " &8- &r&o/ct [player] &r&7| containertrust&r\n" +
                    " &8- &r&o/trust [player] &r&7| trust&r\n" +
                    " &8- &r&o/untrust [player] &r&7| untrust&r\n" +
                    " &8- &r&o/transferclaim [player] &r&7| transfer claim&r\n" +
                    " &8- &r&o/abandonclaim &r&7| abandon a claim&r"));

            return true;
        }

        String claimID = plugin.cm().getClaim(player.getLocation());
        if(claimID.equalsIgnoreCase("none")) {
            Block b = player.getLocation().getBlock();
            int x1 = b.getX() - 2;
            int z1 = b.getZ() - 2;
            int x2 = b.getX() + 2;
            int z2 = b.getZ() + 2;
            if(plugin.cm().createNewClaim(x1, z1, x2, z2, player.isOp(), player.getWorld().getName(), player))
                TextUtil.sendClickableCommand(player,
                        TextUtil.convertColor("&7Use &a&o/claim help&r&7 to see available actions."),
                        "/claim help",
                        "Run /claim help");
            return true;
        }
        else if(args.length == 0) {
            PacketManager.highlightClaim(player, claimID, false);
            player.sendMessage(plugin.cm().toString(player, claimID, false));
            TextUtil.sendClickableCommand(player,
                    TextUtil.convertColor("&7Click here to expand subdivision info"),
                    "/claim subdivisions",
                    "Run /claim subdivisions");
            return true;
        }
        else if(args[0].equalsIgnoreCase("subdivisions")) {
            PacketManager.highlightClaim(player, claimID, false);
            player.sendMessage(plugin.cm().toString(player, claimID, true));
            return true;
        }

        return true;
    }
}
