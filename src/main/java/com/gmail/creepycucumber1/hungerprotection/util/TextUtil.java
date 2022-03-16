package com.gmail.creepycucumber1.hungerprotection.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;

public class TextUtil {

    public static final List<String> MESSAGES = List.of(
            "&cYou don't have permissions in this subdivision",
            "&cYou don't own this claim",
            "&cYou don't have build permissions in this claim",
            "&cYou don't have container permissions in this claim.",
            "&cYou don't have access permissions in this claim."
    );

    public static String convertColor(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String toRainbow(String s) {
        String result = "";

        java.util.List<String> codes = List.of("&4", "&c", "&6", "&e", "&2", "&a", "&b", "&3", "&9", "&d", "&5");
        for(int i = 0; i < s.length(); i++) {
            if(s.substring(i, i + 1) != " ")
                result += codes.get(i % codes.toArray().length);
            result += s.substring(i, i + 1);
        }

        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static String getSuffix(String n) {
        String suffix = "th";
        if(n.length() > 1) {
            if(n.substring(1).equalsIgnoreCase("1") && !n.substring(0, 1).equalsIgnoreCase("1"))
                suffix = "st";
            else if(n.substring(1).equalsIgnoreCase("2") && !n.substring(0, 1).equalsIgnoreCase("1"))
                suffix = "nd";
            else if(n.substring(1).equalsIgnoreCase("3") && !n.substring(0, 1).equalsIgnoreCase("1"))
                suffix = "th";
        }
        else {
            if(n.equalsIgnoreCase("1"))
                suffix = "st";
            else if(n.equalsIgnoreCase("2"))
                suffix = "nd";
            else if(n.equalsIgnoreCase("3"))
                suffix = "th";
        }
        return suffix;
    }

    public static String convertArray(Object[] args){
        return convertArray(args, 0);
    }

    public static String convertArray(Object[] args, int start){
        if(args.length==0) return "";
        StringBuilder sb = new StringBuilder();

        for(int i = start; i < args.length; i++){
            sb.append(args[i].toString());
            sb.append(" ");
        }

        return sb.substring(0, sb.length()-1);
    }

    public static void sendClickableCommand(Player player, String message, String command, String hover) {
        TextComponent component = new TextComponent(TextUtil.convertColor(message));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        player.spigot().sendMessage(component);
    }

    public static void sendActionBarMessage(Player p, String message) {
        message = convertColor(message);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

}
