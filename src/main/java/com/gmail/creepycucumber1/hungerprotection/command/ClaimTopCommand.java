package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.text.DecimalFormat;
import java.util.*;

public class ClaimTopCommand extends CommandBase {
    public ClaimTopCommand(HungerProtection plugin) {
        super(plugin, "claimtop", "Get a leaderboard of the top claimers on the server", "");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }

        HashMap<Double, String> map = new HashMap<>(); //blocks, player name
        double area;
        long total = 0;
        for(OfflinePlayer p : plugin.getPlayerManager().getPlayers()) {
            area = 0.0;
            for(String claimID : plugin.cm().getClaims(p)) {
                if(plugin.cm().getIsAdmin(claimID)) continue;
                BoundingBox box = plugin.cm().getBoundingBox(claimID);
                area += (int) box.getWidthX() * (int) box.getWidthZ();
                total += (long) area;
            }
            while(map.containsKey(area)) area += Math.random(); //avoid overwriting
            map.put(area, p.getName());
        }
        ArrayList<Double> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        Collections.reverse(list);

        int page = 0;
        if(args.length > 0)
            try {
                page = Math.abs(Integer.parseInt(args[0]));
            } catch (NumberFormatException ignored) {}
        if(page > ((list.size() - 1) / 10) + 1) {
            player.sendMessage(TextUtil.convertColor("&7There aren't yet " + page + " pages of this " +
                    (((list.size() - 1) / 10) + 1) + "-page leaderboard!"));
            return true;
        }
        if(page == 0) page = 1;
        int index = (page - 1) * 10;

        player.sendMessage(TextUtil.convertColor("&2&lLeaderboard:&r&2 Area Claimed " +
                "&7(page " + page + " of " + (((list.size() - 1) / 10) + 1) + ")\n"));
        DecimalFormat d = new DecimalFormat("###,###,###,###,###");
        if(page == 1) player.sendMessage(TextUtil.convertColor("&7&oServer total: " + d.format(total) + " blocks"));
        for(int i = index; i < index + 10; i++) {
            if(i > list.size() - 1) return true;
            player.sendMessage(TextUtil.convertColor("&a" + (i + 1) + ". &7" + map.get(list.get(i)) + "&8 - &f" + d.format((int) Math.floor(list.get(i))) + " blocks"));
        }

        return true;
    }
}
