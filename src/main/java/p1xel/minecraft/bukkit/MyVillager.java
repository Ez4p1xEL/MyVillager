package p1xel.minecraft.bukkit;

import com.tcoded.folialib.FoliaLib;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import p1xel.minecraft.bukkit.commands.Cmd;
import p1xel.minecraft.bukkit.commands.TabList;
import p1xel.minecraft.bukkit.listeners.InteractListener;
import p1xel.minecraft.bukkit.listeners.SelectionMode;
import p1xel.minecraft.bukkit.listeners.UserCreation;
import p1xel.minecraft.bukkit.tools.spigotmc.UpdateChecker;
import p1xel.minecraft.bukkit.utils.Cache;
import p1xel.minecraft.bukkit.utils.Config;
import p1xel.minecraft.bukkit.utils.Locale;
import p1xel.minecraft.bukkit.tools.bstats.Metrics;

import java.util.Arrays;
import java.util.List;

public class MyVillager extends JavaPlugin {

    private static MyVillager instance;
    public static MyVillager getInstance() { return instance; }
    private static FoliaLib foliaLib;
    public static FoliaLib getFoliaLib() {return foliaLib;}

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
        foliaLib = new FoliaLib(this);
        cache = new Cache();
        saveDefaultConfig();
        updateConfig();
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
        if (Config.getBool("check-update")) {
            new UpdateChecker(this, 124216).getVersion(version -> {
                if (this.getDescription().getVersion().equals(version)) {
                    getLogger().info(Locale.getMessage("update-check.latest"));
                } else {
                    getLogger().info(Locale.getMessage("update-check.outdate"));
                }
            });
        }
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

    private final List<String> protections = Arrays.asList("player","zombie","pillager", "falling-block","suffocation", "fall", "fire", "explode", "poison");

    private void updateConfig() {
        FileConfiguration config = getConfig();
        int version = config.getInt("configuration");

        // 1.2.0
        if (version < 2) {
            config.set("configuration", 2);
            for (String protection : protections) {
                config.set("protection." + protection, true);
            }
        }

        saveConfig();
    }


}
