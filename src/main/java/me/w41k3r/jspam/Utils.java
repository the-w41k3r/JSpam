package me.w41k3r.jspam;

import net.md_5.bungee.api.ChatColor;

public class Utils {



    public static String msg(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getStr(String key) {
        JSpam plugin = JSpam.getInstance();
        String value = plugin.getConfig().getString("prefix") + plugin.getConfig().getString(key);
        if (value != null) {
            return ChatColor.translateAlternateColorCodes('&', value);
        }
        return null;
    }


}