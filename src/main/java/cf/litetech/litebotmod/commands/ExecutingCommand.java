package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.connection.EventBuilder;
import cf.litetech.litebotmod.connection.RequestBuilder;
import cf.litetech.litebotmod.connection.rpc.ServerCommandHandler;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.*;
import java.util.stream.Collectors;

public class ExecutingCommand {
    private static final SimpleCommandExceptionType INVALID_ARGUMENT = new SimpleCommandExceptionType(
            new LiteralText("This argument is not valid! You must choose an argument that has been suggested"));
    public static final HashMap<String, ExecutingCommand> EXECUTING_COMMANDS = new HashMap<>();
    private final CommandContext<ServerCommandSource> context;
    private final ServerCommandHandler.ServerCommandHandlerDeserializer command;
    private final List<String> argumentNames;
    private HashMap<String, Object> arguments;
    private HashMap<String, String> serializedArguments;
    private final HashMap<String, String> validatedArguments;
    private final String commandName;

    ExecutingCommand(ServerCommandHandler.ServerCommandHandlerDeserializer command, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        this.command = command;
        this.context = context;
        this.commandName = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(LiteralCommandNode.class))
                .map(n -> n.getNode().getName()).collect(Collectors.joining("."));
        this.argumentNames = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(ArgumentCommandNode.class))
                .map(ParsedCommandNode::getNode).
                        map(CommandNode::getName).collect(Collectors.toList());
        this.processArguments();
        this.validatedArguments = this.validateArguments();
    }

    public int resolve() throws CommandSyntaxException {
        if (CommandHook.getRegisteredHooks().containsKey(this.commandName)) {
            EXECUTING_COMMANDS.put(this.commandName, this);

            CommandHook.getRegisteredHooks().get(this.commandName).beforeInvoke(this);
        }

        return new EventBuilder(EventBuilder.Events.COMMAND)
                .setName(this.commandName)
                .setPlayer(this.context.getSource().getPlayer())
                .setArgs(new HashMap<>(this.validatedArguments))
                .dispatchEvent();
    }

    public static void callAfterInvoke(String name, HashMap<String, Object> args) {
        if (!EXECUTING_COMMANDS.containsKey(name) && !CommandHook.getRegisteredHooks().containsKey(name)) return;

        CommandHook.getRegisteredHooks().get(name).afterInvoke(EXECUTING_COMMANDS.get(name));
        CommandHook.getRegisteredHooks().get(name).afterInvoke(EXECUTING_COMMANDS.get(name), args);
        EXECUTING_COMMANDS.remove(name);
    }

    public HashMap<String, String> validateArguments() throws IllegalArgumentException, CommandSyntaxException {
        HashMap<String, String> validatedArguments = new HashMap<>();
        for (String argName : this.serializedArguments.keySet()) {
            ServerCommandHandler.ServerCommandHandlerDeserializer.Argument arg = this.command.getArgumentFromName(argName) != null ?
                    this.command.getArgumentFromName(argName) : ServerCommandHandler.ServerCommandHandlerDeserializer.getArgumentFromName(argName, this.commandName);

            assert arg != null;
            String serializedArgument = this.serializedArguments.get(arg.name);

            if (!arg.type.equals("StrictSuggesterArgument")) {
                validatedArguments.put(arg.name, serializedArgument);
                continue;
            }

            List<String> suggestions = Command.getSuggestions(this.context, arg.name);
            if (!suggestions.contains(serializedArgument)) {
                throw INVALID_ARGUMENT.create();
            }

            validatedArguments.put(arg.name, serializedArgument);
        }

        return validatedArguments;
    }


    public CommandContext<ServerCommandSource> getContext() {
        return context;
    }

    public HashMap<String, Object> getArguments() {
        return arguments;
    }

    public HashMap<String, String> getSerializedArguments() {
        return serializedArguments;
    }

    public String getCommandName() {
        return commandName;
    }

    private void processArguments() throws CommandSyntaxException {
        HashMap<String, Object> rawArguments = new HashMap<>();
        HashMap<String, String> serializedArguments = new HashMap<>();
        for (String argName : this.argumentNames) {
            ServerCommandHandler.ServerCommandHandlerDeserializer.Argument arg = this.command.getArgumentFromName(argName) != null ?
                    this.command.getArgumentFromName(argName) : ServerCommandHandler.ServerCommandHandlerDeserializer.getArgumentFromName(argName, this.commandName);

            switch (arg.type) {
                case "StringArgument":
                case "SuggesterArgument":
                case "StrictSuggesterArgument":
                    rawArguments.put(arg.name, StringArgumentType.getString(this.context, arg.name));
                    serializedArguments.put(arg.name, StringArgumentType.getString(this.context, arg.name));
                    break;
                case "MessageArgument":
                    rawArguments.put(arg.name, MessageArgumentType.getMessage(this.context, arg.name));
                    serializedArguments.put(arg.name, MessageArgumentType.getMessage(this.context, arg.name).getString());
                    break;
                case "IntegerArgument":
                    rawArguments.put(arg.name, IntegerArgumentType.getInteger(this.context, arg.name));
                    serializedArguments.put(arg.name, Integer.toString(IntegerArgumentType.getInteger(this.context, arg.name)));
                    break;
                case "BooleanArgument":
                    rawArguments.put(arg.name, BoolArgumentType.getBool(this.context, arg.name));
                    serializedArguments.put(arg.name, Boolean.toString(BoolArgumentType.getBool(this.context, arg.name)));
                    break;
                case "PlayerArgument":
                    rawArguments.put(arg.name, EntityArgumentType.getPlayer(this.context, arg.name));
                    serializedArguments.put(arg.name, Serializers.serializePlayer(EntityArgumentType.getPlayer(this.context, arg.name)));
                    break;
                case "BlockPosArgument":
                    rawArguments.put(arg.name, BlockPosArgumentType.getBlockPos(this.context, arg.name));
                    serializedArguments.put(arg.name, Serializers.serializeBlockPos(BlockPosArgumentType.getBlockPos(this.context, arg.name)));
                    break;
                case "DimensionArgument":
                    rawArguments.put(arg.name, DimensionArgumentType.getDimensionArgument(this.context, arg.name));
                    serializedArguments.put(arg.name, Serializers.serializeDimension(DimensionArgumentType.getDimensionArgument(this.context, arg.name)));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + arg.type);
            }
        }

        this.arguments = rawArguments;
        this.serializedArguments = serializedArguments;
    }
}
