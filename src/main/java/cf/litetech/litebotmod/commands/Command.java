package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.RequestBuilder;
import cf.litetech.litebotmod.connection.rpc.ServerCommandHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class Command {
    private static final HashMap<String, ArgumentType<?>> ARGUMENT_TYPES = new HashMap<>();
    public ServerCommandHandler.ServerCommandHandlerDeserializer data;

    private Command(ServerCommandHandler.ServerCommandHandlerDeserializer data) {
        this.data = data;
    }

    static {
        ARGUMENT_TYPES.put("StringArgument", StringArgumentType.string());
        ARGUMENT_TYPES.put("SuggesterArgument", StringArgumentType.string());
        ARGUMENT_TYPES.put("StrictSuggesterArgument", StringArgumentType.string());
        ARGUMENT_TYPES.put("MessageArgument", MessageArgumentType.message());
        ARGUMENT_TYPES.put("IntegerArgument", IntegerArgumentType.integer());
        ARGUMENT_TYPES.put("BooleanArgument", BoolArgumentType.bool());
        ARGUMENT_TYPES.put("PlayerArgument", EntityArgumentType.players());
        ARGUMENT_TYPES.put("BlockPosArgument", BlockPosArgumentType.blockPos());
        ARGUMENT_TYPES.put("DimensionArgument", DimensionArgumentType.dimension());
    }

    public static Command register(CommandDispatcher<ServerCommandSource> dispatcher, ServerCommandHandler.ServerCommandHandlerDeserializer commandData) {
        Command COMMAND = new Command(commandData);
        dispatcher.register(COMMAND.buildCommand(commandData));

        return COMMAND;
    }

    public LiteralArgumentBuilder<ServerCommandSource> buildCommand(ServerCommandHandler.ServerCommandHandlerDeserializer command) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.name);
        commandBuilder.requires(source -> (
                LiteBotMod.getConnection().isOpen() && CommandRegisters.containsCommand(this.data) &&
                        source.hasPermissionLevel(CommandRegisters.getUpdatedData(this.data).OPLevel)
        ));

        if (!command.arguments.isEmpty()) {
            RequiredArgumentBuilder<ServerCommandSource, ?> argBuilder =  buildArgs(command, commandBuilder, command.arguments.listIterator());

            if (command.subs != null) {
                for (ServerCommandHandler.ServerCommandHandlerDeserializer sub : command.subs) {
                    argBuilder.then(buildCommand(sub));
                }
            }

            commandBuilder.then(argBuilder);
        } else {
            commandBuilder.executes(context -> executeCommand(command, context));

            if (command.subs != null) {
                for (ServerCommandHandler.ServerCommandHandlerDeserializer sub : command.subs) {
                    commandBuilder.then(buildCommand(sub));
                }
            }
        }

        return commandBuilder;
    }

    private RequiredArgumentBuilder<ServerCommandSource, ?> buildArgs(
            ServerCommandHandler.ServerCommandHandlerDeserializer command, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
            ListIterator<ServerCommandHandler.ServerCommandHandlerDeserializer.Argument> argumentIterator) {
        ServerCommandHandler.ServerCommandHandlerDeserializer.Argument arg = argumentIterator.next();
        RequiredArgumentBuilder<ServerCommandSource, ?> curBuilder = CommandManager.argument(arg.name,
                ARGUMENT_TYPES.get(arg.type));

        if (arg.optional) {
            argumentBuilder.executes(context -> executeCommand(command, context));
        }

        ArrayList<String> thing = new ArrayList<>();
        thing.add("hi");
        thing.add("bye");

        if (arg.type.contains("SuggesterArgument")) {
            curBuilder.suggests((context, builder) -> CommandSource.suggestMatching(getSuggestions(context, arg.name), builder));
        }
        if (argumentIterator.hasNext()) {
            curBuilder.then(buildArgs(command, curBuilder, argumentIterator));
        } else {
            curBuilder.executes(context -> executeCommand(command, context));
        }

        return curBuilder;
    }

    public static ArrayList<String> getSuggestions(CommandContext<ServerCommandSource> context, String argName) throws CommandSyntaxException {
        return new RequestBuilder<ArrayList<String>>("rpc")
                .setName("suggester")
                .setPlayer(context.getSource().getPlayer())
                .addArg("command_name", context.getNodes()
                        .stream().filter(n -> n.getNode().getClass().equals(LiteralCommandNode.class))
                        .map(n -> n.getNode().getName()).collect(Collectors.joining(".")))
                .addArg("arg_name", argName)
                .makeRequest();
    }

    private int executeCommand(ServerCommandHandler.ServerCommandHandlerDeserializer command, CommandContext<ServerCommandSource> context)
            throws CommandSyntaxException {
        ExecutingCommand execCommand = new ExecutingCommand(command, context);
        return execCommand.resolve();
    }
}
