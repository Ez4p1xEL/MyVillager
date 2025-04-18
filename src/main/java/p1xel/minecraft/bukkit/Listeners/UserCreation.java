package p1xel.minecraft.bukkit.Listeners;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import p1xel.minecraft.bukkit.MyVillager;
import p1xel.minecraft.bukkit.Utils.VillagerManager;

import java.io.File;
import java.util.Collections;

public class UserCreation implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {

        Player p = e.getPlayer();
        String name = p.getName();
        String uuid = p.getUniqueId().toString();
        if (!VillagerManager.isFileExist(uuid)) {
            VillagerManager.createUser(uuid, name);
            System.out.println("MyVillager user data is created for " + name + " !");
        } else {
            if (!VillagerManager.isCacheExist(uuid)) {
                File folder = new File(MyVillager.getInstance().getDataFolder(), "users");
                File file = new File(folder + File.separator, uuid + ".yml");
                VillagerManager.upload(uuid, file);
            }
        }
        SelectionMode.putPlayerMode(uuid, "none");
        SelectionMode.putPlayerToggle(uuid, false);
        SelectionMode.putPlayerSelection(uuid, Collections.emptyList());

        if (MyVillager.getCache().isExist(uuid)) {
            MyVillager.getCache().remove(uuid);
        }
    }


}
