package p1xel.minecraft.bukkit;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.Commands.Cmd;
import p1xel.minecraft.bukkit.Commands.TabList;
import p1xel.minecraft.bukkit.Listeners.InteractListener;
import p1xel.minecraft.bukkit.Listeners.SelectionMode;
import p1xel.minecraft.bukkit.Listeners.UserCreation;
import p1xel.minecraft.bukkit.Utils.Cache;
import p1xel.minecraft.bukkit.Utils.Locale;
import p1xel.minecraft.bukkit.bStats.Metrics;

public class MyVillager extends JavaPlugin {

    private static MyVillager instance;
    public static MyVillager getInstance() { return instance; }

    public String getLanguage() {
        return getConfig().getString("language");
    }

    private static Cache cache;
    public static Cache getCache() { return cache;}

    private static Economy econ = null;
    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onEnable() {
        instance = this;
        cache = new Cache();
        saveDefaultConfig();
        Locale.createLocaleFile();

        getServer().getPluginCommand("MyVillager").setExecutor(new Cmd());
        getServer().getPluginCommand("MyVillager").setTabCompleter(new TabList());
        getServer().getPluginManager().registerEvents(new UserCreation(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new SelectionMode(), this);

        if (!setupEconomy() ) {
            getLogger().info("Economy function is not loaded due to missing dependency.");
        } else {
            getLogger().info("Economy function is loaded!");
        }

        // Text from https://tools.miku.ac/taag/ (Font: Slant)
        getLogger().info("    __  ___     _    ___ ____          ");
        getLogger().info("   /  |/  /_  _| |  / (_) / /___ _____ ____  _____");
        getLogger().info("  / /|_/ / / / / | / / / / / __ `/ __ `/ _ \\/ ___/");
        getLogger().info(" / /  / / /_/ /| |/ / / / / /_/ / /_/ /  __/ /    ");
        getLogger().info("/_/  /_/\\__, / |___/_/_/_/\\__,_/\\__, /\\___/_/     ");
        getLogger().info("       /____/                  /____/             ");
        getLogger().info("Plugin is enabled!");

        int pluginId = 25531;
        new Metrics(this, pluginId);
    }

    public boolean isDependencyEnabled(String pluginName) {
        return getServer().getPluginManager().getPlugin(pluginName) != null && getConfig().getBoolean("hooks."+pluginName);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }


}
