package net.wechandoit.src.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String chat(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String removeColor(String message)
    {
        return ChatColor.stripColor(message);
    }

    public static String getFormattedType(String typeName) {
        if(typeName.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            return typeName;

        return format(typeName);
    }

    public static String format(String type){
        StringBuilder name = new StringBuilder();

        type = type.replace(" ", "_");

        for (String section : type.split("_")) {
            name.append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase()).append(" ");
        }

        return name.substring(0, name.length() - 1);
    }
}
