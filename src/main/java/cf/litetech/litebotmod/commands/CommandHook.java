package cf.litetech.litebotmod.commands;


import java.util.HashMap;

public abstract class CommandHook {
    private static final HashMap<String, CommandHook> REGISTERED_HOOKS = new HashMap<>();

    public void register(String name, CommandHook hook) {
        REGISTERED_HOOKS.put(name, hook);
    };

    public static HashMap<String, CommandHook> getRegisteredHooks() {
        return REGISTERED_HOOKS;
    }

    public void beforeInvoke(ExecutingCommand command) {};

    public void afterInvoke(ExecutingCommand command) {};

    public void afterInvoke(ExecutingCommand command, HashMap<String, Object> args) {};
}
