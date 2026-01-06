package p1xel.minecraft.bukkit.commands;

import p1xel.minecraft.bukkit.commands.admin.*;
import p1xel.minecraft.bukkit.commands.group.*;

import java.util.HashMap;

public class CommandRegistry {

    private static final HashMap<String, Command> commands = new HashMap<>() {
        {
            put("claim", new ClaimCommand());
            put("lock", new LockCommand());
            put("info", new InfoCommand());
            put("list", new ListCommand());
            put("group", new GroupCommand());
            put("group create", new GroupCreateCommand());
            put("group delete", new GroupDeleteCommand());
            put("group info", new GroupInfoCommand());
            put("group set", new GroupSetCommand());
            put("group unset", new GroupUnsetCommand());
            put("group set player", new GroupPlayerAddCommand());
            put("group unset player", new GroupPlayerRemoveCommand());
            put("group set villager", new GroupVillagerAddCommand());
            put("group unset villager", new GroupVillagerRemoveCommand());
            put("admin", new AdminCommand());
            put("admin lock", new AdminLockCommand());
            put("admin remove", new AdminRemoveCommand());
            put("reload", new ReloadCommand());
        }
    };

    public static HashMap<String, Command> getCommands() {
        return commands;
    }

}
