package com.gmail.creepycucumber1.hungerprotection.command;

import com.gmail.creepycucumber1.hungerprotection.HungerProtection;
import com.gmail.creepycucumber1.hungerprotection.items.PwarpGUI;
import com.gmail.creepycucumber1.hungerprotection.util.TextUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PwarpCommand extends CommandBase {
    public PwarpCommand(HungerProtection plugin) {
        super(plugin, "pwarp", "Use the player warps system", "", "");
    }

    private final List<String> blockedNames = Arrays.asList("nigger", "faggot", "nigga", "burn jews", "fuck", "3p4ijtgu35h357nyb4576");

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if(!(sender instanceof Player)) {
            sender.sendMessage(TextUtil.convertColor("&cYou must be a player to use this command!"));
            return true;
        }

        HashMap<String, String> names = new HashMap<>(); //warp name, player name
        for(OfflinePlayer player : plugin.getPlayerManager().getPlayers()) {
            ArrayList<String> warp = plugin.getPlayerManager().getPwarp(player);
            try {
                names.put(warp.get(0), player.getName());
            } catch (IndexOutOfBoundsException ignored) {}
        }

        Player player = (Player) sender;
        if(args.length == 0) {
            plugin.getGuiManager().openGUI(player, new PwarpGUI(plugin, player, 0));
            return true;
        }

        if(args[0].equalsIgnoreCase("set")) {
            if(args.length < 2) {
                player.sendMessage(TextUtil.convertColor("&cPlease specify a name for the warp. Example: &o/pwarp set [warp name]"));
                return true;
            }
            StringBuilder warpName = new StringBuilder();
            for(int i = 1; i < args.length - (args[args.length - 1].equalsIgnoreCase("confirm") ? 1 : 0); i++)
                warpName.append(args[i]).append(" ");
            String name = warpName.substring(0, warpName.toString().length() - 1);

            //length, redundancy, and vulgarity checks
            if(name.length() > 30) {
                player.sendMessage(TextUtil.convertColor("&cPlease choose a more concise name."));
                return true;
            }
            else if(name.length() < 2) {
                player.sendMessage(TextUtil.convertColor("&cWhat is \"&7" + name + "&c\" even supposed to mean?"));
                return true;
            }
            for(String blocked : blockedNames) {
                if(name.toLowerCase().contains(blocked)) {
                    player.sendMessage(TextUtil.convertColor("&cPlease choose a better name."));
                    return true;
                }
            }
            for(String otherName : names.keySet()) {
                if(otherName.equalsIgnoreCase(name)) {
                    player.sendMessage(TextUtil.convertColor("&cGreat minds think alike! There is already a warp of that name."));
                    return true;
                }
            }
            if(name.equalsIgnoreCase("random") || name.equalsIgnoreCase("sd") ||
                    name.toLowerCase().startsWith("set ") || name.toLowerCase().startsWith("remove ") ||
                    name.toLowerCase().startsWith("n3xt") || name.toLowerCase().startsWith("pr3vious") ||
                    name.equalsIgnoreCase("1nfo")) {
                player.sendMessage(TextUtil.convertColor("&cThat name is reserved."));
                return true;
            }

            //console-issued confirm
            if(args.length >= 3 && args[args.length - 1].equalsIgnoreCase("confirm")) {
                plugin.getVault().withdrawPlayer(player, 5000);
                plugin.getPlayerManager().setPwarp(player, player.getLocation(), name, false);
                player.sendMessage(TextUtil.convertColor("&aSuccessfully set the warp \"&7" + name + "&a\" to here!"));
                return true;
            }
            //price check
            if(plugin.getVault().getBalance(player) < 5000) {
                player.sendMessage(TextUtil.convertColor("&cYou must have $5000 to set a warp."));
                return true;
            }
            sendClickableCommand(player, TextUtil.convertColor("&aAre you sure you want to set your warp to \"&7" + name +
                            "&a\" here for $5000? Click here to confirm."),
                    "/pwarp set " + name + " confirm", "Confirm new warp");
            return true;
        } else if(args[0].equalsIgnoreCase("remove")) {
            try {
                String name = plugin.getPlayerManager().getPwarp(player).get(0);
                plugin.getPlayerManager().setPwarp(player, player.getLocation(), "", true);
                player.sendMessage(TextUtil.convertColor("&aWarp \"&7" + name + "&a\" successfully removed!"));
            } catch (IndexOutOfBoundsException e) {
                player.sendMessage(TextUtil.convertColor("&aYou didn't have a warp set, so none was removed."));
            }
            return true;
        }

        //is warping to another warp
        for(OfflinePlayer p : plugin.getPlayerManager().getPlayers()) {
            ArrayList<String> pwarp = plugin.getPlayerManager().getPwarp(p);
            if(pwarp.size() == 0) continue;

            StringBuilder warpName = new StringBuilder();
            for(int i = 0; i < args.length; i++) //no confirm needed so no -1 conditional
                warpName.append(args[i]).append(" ");
            String name = warpName.substring(0, warpName.toString().length() - 1);

            if(pwarp.get(0).equalsIgnoreCase(name)) {
                World world = Bukkit.getWorld(pwarp.get(1));
                double x = Double.parseDouble(pwarp.get(2));
                double y = Double.parseDouble(pwarp.get(3));
                double z = Double.parseDouble(pwarp.get(4));
                plugin.getTeleporter().teleport(player, world, x, y, z);
                return true;
            }
        }
        player.sendMessage(TextUtil.convertColor("&c\"&7" + args[0] + "&c\" doesn't match any existing warp name."));

        return true;
    }

    public void sendClickableCommand(Player player, String message, String command, String hover) {
        TextComponent component = new TextComponent(TextUtil.convertColor(message));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        player.spigot().sendMessage(component);
    }
}
