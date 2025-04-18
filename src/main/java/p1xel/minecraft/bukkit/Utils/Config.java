package p1xel.minecraft.bukkit.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import p1xel.minecraft.bukkit.MyVillager;

import java.util.List;

public class Config {

    public static boolean getBool(String path) {
        return MyVillager.getInstance().getConfig().getBoolean(path);
    }

    public static int getInt(String path) {
        return MyVillager.getInstance().getConfig().getInt(path);
    }

    public static double getDouble(String path) {
        return MyVillager.getInstance().getConfig().getDouble(path);
    }

    public static String getString(String path) {
        return MyVillager.getInstance().getConfig().getString(path);
    }

    public static List<String> getStringList(String path) {
        return MyVillager.getInstance().getConfig().getStringList(path);
    }

    public static ConfigurationSection getConfigurationSection(String path) {
        return MyVillager.getInstance().getConfig().getConfigurationSection(path);
    }

}
