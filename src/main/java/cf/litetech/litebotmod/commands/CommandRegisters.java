package cf.litetech.litebotmod.commands;

import cf.litetech.litebotmod.LiteBotMod;
import cf.litetech.litebotmod.connection.ResponseData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class CommandRegisters {
    private static List<ResponseData.CommandResponse> commandData;

    private static final SimpleCommandExceptionType INVALID_ARGUMENT = new SimpleCommandExceptionType(
            new LiteralText("This argument is not valid! You must choose an argument that has been suggested"));

    private static final SimpleCommandExceptionType NO_ARGUMENT = new SimpleCommandExceptionType(
            new LiteralText("Invalid argument entry!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        if (commandData != null) {
            for (ResponseData.CommandResponse command : commandData) {
                dispatcher.register(buildCommand(command));
            }
        }

        if (LiteBotMod.getServer() != null) {
            for (ServerPlayerEntity player : LiteBotMod.getServer().getPlayerManager().getPlayerList()) {
                LiteBotMod.getServer().getPlayerManager().sendCommandTree(player);
            }
        }
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildCommand(ResponseData.CommandResponse command) {
        LiteralArgumentBuilder<ServerCommandSource> commandBuilder = CommandManager.literal(command.name);
        commandBuilder.requires(source -> LiteBotMod.getConnection().isOpen() && command.OPLevel >= 0 && source.hasPermissionLevel(command.OPLevel));

        if (command.arguments != null) {
            commandBuilder.then(buildArgs(command, commandBuilder, command.arguments.listIterator()));
        } else {
            commandBuilder.executes(context -> executeCommand(command, context));
        }

        if (command.subs != null) {
            for (ResponseData.CommandResponse sub : command.subs) {
                commandBuilder.then(buildCommand(sub));
            }
        }

        return commandBuilder;
    }

    private static RequiredArgumentBuilder<ServerCommandSource, ?> buildArgs(
            ResponseData.CommandResponse command, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
            ListIterator<ResponseData.CommandResponse.Argument> argumentIterator) {
        ResponseData.CommandResponse.Argument arg = argumentIterator.next();
        RequiredArgumentBuilder<ServerCommandSource, ?> curBuilder = CommandManager.argument(arg.name, getArgumentType(arg.type));

        if (arg.optional) {
            argumentBuilder.executes(context -> executeCommand(command, context));
        }

        if (arg.type.equals("SuggesterArgument") || arg.type.equals("StrictSuggesterArgument")) {
            curBuilder.suggests((context, builder) -> CommandSource.suggestMatching(
                    getSuggestions(command, context, arg), builder));
        }
        if (argumentIterator.hasNext()) {
            curBuilder.then(buildArgs(command, curBuilder, argumentIterator));
        } else {
            curBuilder.executes(context -> executeCommand(command, context));
        }

        return curBuilder;
    }

    private static int executeCommand(ResponseData.CommandResponse command, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        List<String> literalNodes = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(LiteralCommandNode.class))
                .map(n -> n.getNode().getName()).collect(Collectors.toList());


        List<String> argumentNames = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(ArgumentCommandNode.class))
                .map(ParsedCommandNode::getNode).
                        map(CommandNode::getName).collect(Collectors.toList());

        List<String> arguments = new ArrayList<>();

        for (String argName : argumentNames) {
            arguments.add(validatedArgument(argName, command, context));
        }

        String commandName = String.join(".", literalNodes);
        LiteBotMod.getBridge().sendCommand(commandName, context.getSource().getPlayer().getUuidAsString(), arguments);

        return 0;
    }

    private static String validatedArgument(String argName, ResponseData.CommandResponse command, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ResponseData.CommandResponse.Argument arg = command.getArgumentFromName(argName);
        String argVal = getArgument(arg, context);

        if (!arg.type.equals("StrictSuggesterArgument")) {
            return argVal;
        }

        List<String> suggestions = getSuggestions(command, context, arg);

        if (!suggestions.contains(argVal)) {
            throw INVALID_ARGUMENT.create();
        }

        return argVal;
    }

    private static List<String> getSuggestions(ResponseData.CommandResponse command, CommandContext<ServerCommandSource> context, ResponseData.CommandResponse.Argument arg) throws CommandSyntaxException {
        List<String> literalNodes = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(LiteralCommandNode.class))
                .map(n -> n.getNode().getName()).collect(Collectors.toList());

        List<String> argumentNames = context.getNodes()
                .stream().filter(n -> n.getNode().getClass().equals(ArgumentCommandNode.class))
                .map(ParsedCommandNode::getNode).
                        map(CommandNode::getName).collect(Collectors.toList());

        List<String> arguments = new ArrayList<>();

        for (String argName : argumentNames) {
            arguments.add(getArgument(command.getArgumentFromName(argName), context));
        }

        String commandName = String.join(".", literalNodes);

        return LiteBotMod.getBridge().fetchSuggestions(
                commandName, context.getSource().getPlayer().getUuidAsString(), arg.name, arguments);
    }

    private static ArgumentType<?> getArgumentType(String typeName) {
        switch (typeName) {
            case "StringArgument":
            case "SuggesterArgument":
            case "StrictSuggesterArgument":
                return StringArgumentType.string();
            case "MessageArgument":
                return MessageArgumentType.message();
            case "IntegerArgument":
                return IntegerArgumentType.integer();
            case "BooleanArgument":
                return BoolArgumentType.bool();
            case "PlayerArgument":
                return EntityArgumentType.player();
        }

        return StringArgumentType.string();
    }

    private static String getArgument(ResponseData.CommandResponse.Argument arg, CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        switch (arg.type) {
            case "StringArgument":
            case "SuggesterArgument":
            case "StrictSuggesterArgument":
                return StringArgumentType.getString(context, arg.name);
            case "MessageArgument":
                return MessageArgumentType.getMessage(context, arg.name).getString();
            case "IntegerArgument":
                return Integer.toString(IntegerArgumentType.getInteger(context, arg.name));
            case "BooleanArgument":
                return Boolean.toString(BoolArgumentType.getBool(context, arg.name));
        }

        throw NO_ARGUMENT.create();
    }

    public static void setCommandData(List<ResponseData.CommandResponse> commandData) {
        CommandRegisters.commandData = commandData;

    }

}
