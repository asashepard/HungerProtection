package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.event.PacketManager;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import net.minecraft.server.packs.repository.Pack;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand extends CommandBase {
    public ClaimCommand(HungerProtection plugin) {
        super(plugin, "claim", "Instantly claim a 5x5 plot of land", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }
        Player player = (Player) sender;

        if(args.length == 0) {
            String claimID = plugin.cm().getClaim(player.getLocation());
            if(claimID.equalsIgnoreCase("none")) {
                Block b = player.getLocation().getBlock();
                int x1 = b.getX() - 2;
                int y1 = b.getY() - 2;
                int x2 = b.getX() + 2;
                int y2 = b.getY() + 2;
                if(plugin.cm().createNewClaim(x1, y1, x2, y2, player.isOp(), player.getWorld().getName(), player))
                    player.sendMessage(TextUtil.convertColor("&eUse &o/claim help&r&e to see available actions."));

                return true;
            }
            PacketManager.highlightClaim(player, claimID, false);
            player.sendMessage(plugin.cm().toString(player, claimID));
        }

        player.sendMessage(TextUtil.convertColor("&3&lClaim Commands: &r\n" +
                " - &o/claim &r&7| create a new 5x5 claim&r\n" +
                " - &o/claimtool &r&7| get tools to manage claims&r\n" + //todo
                " - &o/claims &r&7| list your claims&r\n" +
                " - &o/claimtop &r&7| get server leaderboard&r\n" +
                " - &o/buyclaimblocks [#] &r&7| buy claim blocks&r\n" +
                " - &o/at [player] &r&7| accesstrust&r\n" +
                " - &o/ct [player] &r&7| containertrust&r\n" +
                " - &o/trust [player] &r&7| trust&r\n" +
                " - &o/untrust [player] &r&7| untrust&r\n" +
                " - &o/transferclaim [player] &r&7| transfer claim&r\n" +
                " - &o/abandonclaim &r&7| abandon a claim&r\n"));

        return true;
    }
}
