package cf.litetech.litebotmod.commands;

import net.minecraft.server.command.ServerCommandSource;

import java.util.HashMap;

public abstract class CommandHook {
    private static final HashMap<String, CommandHook> REGISTERED_HOOKS = new HashMap<>();

    CommandHook(String name, CommandHook instance) {
        REGISTERED_HOOKS.put(name, instance);
    }

    public void beforeInvoke(ServerCommandSource source, Object[] args) {};

    public void afterInvoke(ServerCommandSource source, Object[] args) {};
}
