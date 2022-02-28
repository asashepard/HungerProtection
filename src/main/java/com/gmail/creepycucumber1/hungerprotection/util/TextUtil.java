package com.gmail.creepycucumber1.hungerprotection.util;

import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class TextUtil {
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
}
