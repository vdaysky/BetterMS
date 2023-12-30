package obfuscate.comand.builder;

import obfuscate.MsdmPlugin;
import obfuscate.comand.CmdContext;
import obfuscate.comand.CommandLevel;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.WrappedSender;
import obfuscate.comand.argument.ArgSet;
import obfuscate.comand.argument.BoolArg;
import obfuscate.comand.argument.CommandArgument;
import obfuscate.comand.argument.ParsedArgument;
import obfuscate.comand.exception.CommandArgArrayParseException;
import obfuscate.comand.exception.CommandArgParseException;
import obfuscate.comand.exception.CommandLevelParseException;
import obfuscate.message.MsgSender;
import obfuscate.permission.Permission;
import obfuscate.util.chat.C;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Stream.concat;


public class FakeCommandRegistry extends BukkitCommand {

    private final CommandHandler handler;
    private static final HashMap<WrappedSender, HashMap<String, Long>> commandExecutionTimes = new HashMap<>();

    public FakeCommandRegistry(CommandHandler handler, String name, String[] aliases) {
        super(name, handler.getDescription(), "", new ArrayList<>(Arrays.asList(aliases)));
        this.handler = handler;
        setUsage(usage(handler, name, true));
    }

    /** Generate help message for parsed command level.
     * Only includes help for current level, not for children
    * **/
    private static String usage(CommandHandler handler, String rootLabel, boolean showPossibleBranching) {

        StringBuilder usage = new StringBuilder(C.cGreen + rootLabel);

        for (CommandArgument<?> required_argument : handler.getDeclaredPositionalArgs().args) {
            String typeName = required_argument.getTypeName();

            if (!required_argument.getStaticOptions().isEmpty()) {
                typeName = C.cGray + "[" + C.cGold +
                        required_argument.getStaticOptions().stream().collect(Collectors.joining(C.cGray + " | " + C.cGold))
                        + C.cGray + "]";
            }

            usage.append(C.cGray + " [").append(
                    C.cGold + required_argument.getName() + C.cGray + ":" + C.cGold + typeName).append(C.cGray + "]");
        }

        for (CommandArgument<?> optional_argument : handler.getDeclaredOptionalArgs().args) {

            String typeName = optional_argument.getTypeName();

            if (!optional_argument.getStaticOptions().isEmpty()) {
                typeName = C.cGray + "[" + C.cGold +
                        optional_argument.getStaticOptions().stream().collect(Collectors.joining(C.cGray + " | " + C.cAqua))
                        + C.cGray + "]";
            }

            usage.append(C.cGray + " (-").append(C.cAqua + optional_argument.getName() +
                    C.cGray + " <" + C.cAqua + typeName + C.cGray  +">").append(C.cGray +")");
        }

        if (showPossibleBranching && handler.getChildren().size() > 0) {
            usage.append(C.cGray + " (");
            String options = handler.getChildren().keySet().stream().collect(Collectors.joining(C.cGray + " | " + C.cGreen));
            usage.append(C.cGreen + options);
            usage.append(C.cGray + ")");
        }

        return usage.toString();
    }

    private static String usage(CommandLevel level) {

        String usagePath = "";

        // validate at least once (root level) and every child level as well
        while (level.getChild() != null) {
            usagePath += usage(level.getHandler(), level.getLabel(), false) + " ";

            level = level.getChild();
        }

        usagePath += usage(level.getHandler(), level.getLabel(), true);
        return usagePath;
    }

    /** Parse command argument value and optionally rise an error */
    private void tryParseAddArgument(
            ArgSet set,
            CommandArgument<?> arg,
            String val,
            boolean isPositional,
            int argIdx,
            boolean raises
    ) throws CommandArgArrayParseException {

        try {
            set.add(arg, val, isPositional);
        } catch (CommandArgParseException e) {
            if (raises) {
                throw new CommandArgArrayParseException(argIdx);
            }
            try {
                set.add(arg, val, isPositional, true);
            } catch (CommandArgParseException e1) {
                throw new RuntimeException("Arguments marked as invalid can't raise validation errors");
            }
        }
    }


    /** Implementation of greedy string consumption that only yields to child commands
     * @return number of consumed args */
    private int consumeUntilChildCommandFound(CommandHandler handler, List<String> args) {
        int argIdx = 0;
        while (argIdx < args.size()) {
            String arg = args.get(argIdx);

            if (handler.getChildren().containsKey(arg)) {
                return argIdx;
            }

            argIdx++;
        }
        return argIdx;
    }

    /**
     * */
    private int populateCommandLevel(
            CommandHandler handler,
            ArgSet required,
            ArgSet optional,
            List<String> args,
            boolean partialParse
    ) throws CommandArgArrayParseException {
        int argIdx = 0;

        boolean raisesParsingErrors = !partialParse;

        // required args are positional
        for (CommandArgument<?> argument : handler.getDeclaredPositionalArgs().args) {
            if (argIdx < args.size()) {

                String arg;
                if (argument.isGreedy()) {
                    int consumedArgCount = consumeUntilChildCommandFound(handler, args.subList(argIdx, args.size()));
                    List<String> consumed = args.subList(argIdx, argIdx + consumedArgCount);
                    argIdx += consumedArgCount - 1; // subtract one so pointer points to last consumed arg,
                    // it is important for tryParseAddArgument to have position of argument that failed,
                    // and not argument that was about get parsed next

                    arg = String.join(" ", consumed);
                } else {
                    arg = args.get(argIdx);
                }
                // if during partial parse we encounter argument that can't be parsed - we ignore everything after
                // and return invalid argument as the only left argument
                tryParseAddArgument(required, argument, arg, true, argIdx, raisesParsingErrors);
                argIdx++;
            }
        }

        // argument consisting of two parts: -X Y,
        // nextArgType is defined only by the first part though
        CommandArgument<?> nextArgType = null;

        // from here we want to grab named optional and positional optional args
        optArgsLoop: for (int afterReqIdx = argIdx; afterReqIdx < args.size(); afterReqIdx++) {

            String arg = args.get(afterReqIdx);

            // sub commands have the highest priority after required positional args. todo: maybe its a bad idea and named optionals should have priority?
            // this means that even if we had a named variable name before, we will be
            // looking for subcommand first, and only if we don't find it we can consider
            // this value a value for named arg.

            // if current arg matches name of child command - terminate in place
            // all args that are left they relate to this next command
            for (String childCmdName : handler.getChildren().keySet()) {
                if (arg.equalsIgnoreCase(childCmdName)) {
                    argIdx = afterReqIdx; // notice that we don't increase by one here, we want arguments to start from subcommand name.
                    // exit the outer loop to stop iterating over the left args. Those args belong to a subcommand
                    // we can afford that because those are optional args, and as you might figure they are not required.
                    break optArgsLoop;
                }
            }

            // we expect to see a value, so grab it and go to next arg
            if (nextArgType != null) {

                // if we managed to successfully grab the arg then we move on to the next one
                tryParseAddArgument(optional, nextArgType, arg, false, afterReqIdx, raisesParsingErrors);
                argIdx = afterReqIdx + 1;
                nextArgType = null;
            }

            // search for named args prefix (-X)
            for (CommandArgument<?> optArgType : handler.getDeclaredOptionalArgs().args) {
                if (arg.equalsIgnoreCase("-" + optArgType.getName())) {

                    // set named arg to null to denote that it is missing value yet, but it is present

                    String value = null;

                    if (optArgType instanceof BoolArg) {
                        value = "true"; // booleans don't really need to have a value
                    }

                    // argument is certainly not positional, because we matched the prefix
                    tryParseAddArgument(optional, optArgType, value, false, afterReqIdx, raisesParsingErrors);
                    nextArgType = optArgType;
                    argIdx = afterReqIdx + 1;

                    break;
                }
            }

            // named arg found above, don't search for anything else
            // as we know value (or a subcommand) must follow the name
            if (nextArgType != null)
                continue;

            // arguments that start with - are prefixes, so it can't be saved as positional arg,
            // leaving it unparsed until it matches
            if (!arg.startsWith("-")) {

                int optArgPosition = 0;
                // search for positional optional args, those have the lowest priority
                for (CommandArgument<?> optArg : handler.getDeclaredOptionalArgs().args) {

                    // if argument index in declaration list does not match current last parsed index
                    // do not process
                    if (optArgPosition != optional.size()) {
                        break;
                    }

                    if (!optional.has(optArg)) {
                        if (optArg.isGreedy()) {
                            int consumedCount = consumeUntilChildCommandFound(handler, args.subList(afterReqIdx, args.size()));
                            List<String> consumed = args.subList(afterReqIdx, afterReqIdx + consumedCount);
                            afterReqIdx += consumedCount - 1; // subtract one so pointer points to last consumed arg,
                            // it is important for tryParseAddArgument to have position of argument that failed,
                            // and not argument that was about get parsed next
                            arg = String.join(" ", consumed);
                            tryParseAddArgument(optional, optArg, arg, true, afterReqIdx, raisesParsingErrors);
                            argIdx = afterReqIdx + 1;
                        } else {
                            tryParseAddArgument(optional, optArg, arg, true, afterReqIdx, raisesParsingErrors);
                            argIdx = afterReqIdx + 1;
                        }
                    }
                    optArgPosition ++;
                }
            }
        }
        return argIdx;
    }

    /**
     * Parse one level of command defined by command handler.
     *
     * @param handler      Command handler to use for parsing
     *                     (contains information about command structure)
     * @param args         List of arguments to parse
     * @param partialParse If true, parsing will stop at first invalid argument.
     *                     Used for parsing knowingly invalid commands while typing them for autocompletion.
     *                     Errors will not be raised in this case.
     * */
    private CommandLevel ParseCommandLevel(
            CommandHandler handler,
            String label,
            List<String> args,
            boolean partialParse
    ) throws CommandLevelParseException {

        ArgSet required = new ArgSet();
        ArgSet optional = new ArgSet();

        int argsLeftIdx;

        try {
            argsLeftIdx = populateCommandLevel(handler, required, optional, args, partialParse);
        } catch (CommandArgArrayParseException e) {
            if (partialParse) {
                var single = Collections.singletonList(args.get(e.getArgIndex()));
                return new CommandLevel(handler, required, optional, null, label, single);
            }
            throw new CommandLevelParseException(
                    new CommandLevel(
                            handler,
                            required,
                            optional,
                            null,
                            label,
                            args.subList(e.getArgIndex(), args.size())
                    )
            );
        }

        List<String> nextCmdArgs = args.subList(argsLeftIdx, args.size());

        CommandHandler next = null;

        if (!nextCmdArgs.isEmpty()) {
            for (String childName : handler.getChildren().keySet()) {
                // check if the first command in the list equals to a name of subcommand
                if (nextCmdArgs.get(0).equalsIgnoreCase(childName)) {
                    next = handler.getChildCommand(childName);
                    break;
                }
            }
        }

        if (next == null) {
            return new CommandLevel(handler, required, optional, null, label, nextCmdArgs);
        }

//        required.savepoint();
//        optional.savepoint();

        try {
            String childLabel = nextCmdArgs.get(0);
            CommandLevel child = ParseCommandLevel(next, childLabel, nextCmdArgs.subList(1, nextCmdArgs.size()), partialParse);
            return new CommandLevel(handler, required, optional, child, label, null);
        } catch (CommandLevelParseException e) {
//            required.rollback();
//            optional.rollback();

            throw new CommandLevelParseException(
                new CommandLevel(handler, required, optional, e.getLevel(), label, null)
            );
        }
    }

    private boolean validateLevelBeforeExecute(final CommandLevel level, ExecutionContext context) {

        String usage = "Usage: " + usage(level);

        CommandLevel nextLevel = level;

        // validate at least once (root level) and every child level as well
        while (nextLevel != null) {
            // not all required arguments are present in this command
            if (!nextLevel.missingRequired().isEmpty()) {
                context.getSender().sendMessage(MsgSender.CMD, usage);
                return false;
            }

            var allArgs = concat(
                    StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                nextLevel.getParsedRequired().values(),
                            Spliterator.ORDERED
                        ), false
                    ),
                    StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                nextLevel.getParsedOptional().values(),
                            Spliterator.ORDERED
                        ), false
                    )
            ).toList();


            // validate all arguments
            for (ParsedArgument<?> argWithValue : allArgs) {

                CommandArgument<?> argDeclaration = argWithValue.getDeclaration();
                Object argValue = argWithValue.getParsedValue();
                String errorMessage = argDeclaration.validate(context, argValue);

                if (errorMessage != null) {
                    context.getSender().sendMessage(MsgSender.CMD, errorMessage);
                    context.getSender().sendMessage(MsgSender.CMD, usage);
                    return false;
                }
            }

            nextLevel = nextLevel.getChild();
        }

        CommandLevel commandLeaf = level.reachLeaf();

        // there are leftover arguments
        if (!commandLeaf.getArgsLeft().isEmpty()) {
            context.getSender().sendMessage(MsgSender.CMD, usage);
            return false;
        }

        // if lowest command in command tree is marked as not implemented,
        // then we should display usage message.
        // for example, it would happen for /game command, which has executor,
        // but requires a subcommand still, since executor is there only
        // to make additional validations.

        // if command does not have executor, or has executor which only contains validation logic,
        // we display usage message
        if (!commandLeaf.getHandler().isImplemented() || !commandLeaf.getHandler().hasExecutor()) {
            context.getSender().sendMessage(MsgSender.CMD, usage);
            return false;
        }

        return true;
    }

    /** Adapter between minecraft command executor and my implementation */
    private boolean parseAndExecute(String label, String[] args, WrappedSender sender) {
        if (handler.isPlayerRequired()) {
            if (sender.getPlayer() == null) {
                sender.sendMessage(MsgSender.CMD, C.cRed + "Command requires player");
                return false;
            }
        }

        // first parse the command tree
        CommandLevel level;
        try {
            level = ParseCommandLevel(handler, label, Arrays.asList(args), false);
        } catch (CommandLevelParseException e) {
            sender.sendMessage(MsgSender.CMD, "Usage: " + usage(e.getLevel()));
            return false;
        }

        var context = new ExecutionContext(level, sender, label);

        boolean valid = validateLevelBeforeExecute(level, context);
        // command was proven to be invalid, stop execution
        if (!valid) {
            return false;
        }

        return executeCommandLevelRecursive(context);
    }

    private long sinceCommandUsage(WrappedSender sender, String commandPath) {

        commandExecutionTimes.putIfAbsent(sender, new HashMap<>());
        HashMap<String, Long> commandTimes = commandExecutionTimes.get(sender);
        return (System.currentTimeMillis() - commandTimes.getOrDefault(commandPath, 0L)) / 1000;
    }

    private boolean executeCommandLevelRecursive(ExecutionContext context) {

        CommandLevel level = context.getLevel();
        WrappedSender sender = context.getSender();
        String label = context.getLabel();

        Permission requiredBasePermission = level.getHandler().getRequiredPermission();

        CmdContext commandContext = level.getHandler().getCommandContext();

        if (commandContext != null) {
            var scopeObj = commandContext.evalForPlayer(sender);
            if (scopeObj != null) {
                context.addScope(scopeObj);
            }
        }

        int throttle = level.getHandler().getThrottleSeconds();
        if (throttle != 0) {
            String commandPath = level.getFullCommandLabel();
            long sinceLastUsage = sinceCommandUsage(sender, commandPath);
            if (sinceLastUsage < throttle) {
                int toWait = throttle - (int) sinceLastUsage;
                sender.sendMessage(MsgSender.CMD, ChatColor.RED + "You can't use this command yet! (" + commandPath + " wait " + toWait + "s)");
                return false;
            }
            System.out.println("Save throttle for " + commandPath + " " + System.currentTimeMillis());
            commandExecutionTimes.get(sender).put(commandPath, System.currentTimeMillis());
        }

        if (!sender.hasPermission(requiredBasePermission, context.getScopes())) {
            sender.sendMessage(MsgSender.CMD, ChatColor.RED + "You don't have permission to do this!");
            return false;
        }

        String failReason = level.getHandler().getCondition().apply(context);

        if (failReason != null) {
            sender.sendMessage(MsgSender.PLUGIN, failReason);
            return false;
        }

        boolean success = level.getHandler().getExecutor().apply(context);

        // stop propagating if any step fails
        if (!success) {
            return false;
        }

        if (level.getChild() != null) {
            var nextContext = new ExecutionContext(level.getChild(), sender, label);
            return executeCommandLevelRecursive(nextContext);
        }

        return true;
    }

    /** Adapter between minecraft command auto completer and my implementation */
    private List<String> executeCompleter(String label, String[] args, WrappedSender sender) {

        if(handler.isPlayerRequired()) {
            if (sender.getPlayer() == null) {
                return List.of("<HINT: Cant execute this command: Player required>");
            }
        }

        try {
            CommandLevel level = ParseCommandLevel(handler, label, Arrays.asList(args), true);

            // we will be autocompleting last command
            while (level.getChild() != null) {
                level = level.getChild();
            }

            Permission requiredPermission = level.getHandler().getRequiredPermission();
            var context = new ExecutionContext(level, sender, label);

            CmdContext commandContext = level.getHandler().getCommandContext();

            if (commandContext != null) {
                var scopeObj = commandContext.evalForPlayer(sender);
                if (scopeObj != null) {
                    context.addScope(scopeObj);
                }
            }

            if (!sender.hasPermission(requiredPermission, context.getScopes())) {
                return List.of("<HINT: Cant execute this command: Permission required>");
            }

            String failReason = handler.getCondition().apply(context);

            if (failReason != null) {
                return List.of("<HINT: Cant execute this command: " + failReason + ">");
            }

            return handler.getCompleter().autocomplete(context);

        } catch (CommandLevelParseException e) {  // shouldn't happen
            return new ArrayList<>();
        }
    }

    @Override
    public @NotNull List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return executeCompleter(alias, args, new WrappedSender(sender));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // TODO: preprocess args to parse " " / replaces spaces

        var chatId = MsdmPlugin.Config.getChatId();
        var message = sender.getName() + " issued a command: " + label + " " + String.join(" ", args);
        MsdmPlugin.getTgBot().execute(
                new SendMessage(chatId, message),
                new Callback<SendMessage, SendResponse>() {
                    @Override
                    public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {

                    }

                    @Override
                    public void onFailure(SendMessage sendMessage, IOException e) {

                    }
                }
        );

        return parseAndExecute(label, args, new WrappedSender(sender));
    }
}
