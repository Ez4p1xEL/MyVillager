package p1xel.minecraft.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.Commands.Cmd;
import p1xel.minecraft.bukkit.Listeners.InteractListener;
import p1xel.minecraft.bukkit.Listeners.SelectionMode;
import p1xel.minecraft.bukkit.Listeners.UserCreation;
import p1xel.minecraft.bukkit.Utils.Locale;

public class MyVillager extends JavaPlugin {

    private static MyVillager instance;
    public static MyVillager getInstance() { return instance; }

    public String getLanguage() {
        return getConfig().getString("language");
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Locale.createLocaleFile();

        getServer().getPluginCommand("MyVillager").setExecutor(new Cmd());
        getServer().getPluginManager().registerEvents(new UserCreation(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new SelectionMode(), this);
    }


}
