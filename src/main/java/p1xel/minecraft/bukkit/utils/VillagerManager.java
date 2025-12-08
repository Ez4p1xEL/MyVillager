package p1xel.minecraft.bukkit.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import p1xel.minecraft.bukkit.MyVillager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VillagerManager {

    static HashMap<String, File> files = new HashMap();
    static HashMap<String, FileConfiguration> yamls = new HashMap();

    public static void createUser(String uuid, String name) {
        File folder = new File(MyVillager.getInstance().getDataFolder(), "users");
        File file = new File(folder + File.separator, uuid + ".yml");
        FileConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException var7) {
                throw new RuntimeException(var7);
            }

            yaml.set(uuid + ".name", name);

            try {
                yaml.save(file);
            } catch (IOException var6) {
                throw new RuntimeException(var6);
            }

            files.put(uuid, file);
            yamls.put(uuid, yaml);
        }

    }

    public static void upload(String uuid, File file) {
        files.put(uuid, file);
        yamls.put(uuid, YamlConfiguration.loadConfiguration(file));
    }


    public static FileConfiguration get(String uuid) {
        return yamls.get(uuid);
    }

    public static void set(String uuid, String path, Object value) {
        File newFile = files.get(uuid);
        FileConfiguration newYaml = yamls.get(uuid);
        newYaml.set(path, value);

        try {
            newYaml.save(newFile);
        } catch (IOException var6) {
            throw new RuntimeException(var6);
        }

        yamls.replace(uuid, newYaml);
    }

    public static boolean isCacheExist(String uuid) {
        return files.get(uuid) != null;
    }

    public static boolean isFileExist(String uuid) {
        File folder = new File(MyVillager.getInstance().getDataFolder(), "users");
        File file = new File(folder + File.separator, uuid + ".yml");
        return file.exists();
    }

    public static boolean isExist(String playerUUID, String villagerUUID) {
        return get(playerUUID).isSet("villagers." + villagerUUID);
    }


}
