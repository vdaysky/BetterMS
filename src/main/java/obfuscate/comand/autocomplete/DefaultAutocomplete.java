package obfuscate.comand.autocomplete;

import obfuscate.MsdmPlugin;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.argument.CommandArgument;
import obfuscate.comand.argument.ParsedArgument;

import java.util.ArrayList;
import java.util.List;

public class DefaultAutocomplete implements CommandAutocomplete {

    public DefaultAutocomplete() {
    }

    private List<String> filterApplicable(String str, List<String> args) {
        List<String> suggestions = new ArrayList<>();

        for (String option : args) {
            if (option.toLowerCase().contains(str.toLowerCase())) {
                suggestions.add(option);
            }
        }
        return suggestions;
    }

    /** suggest child method names that are passed to constructor */
    @Override
    public List<String> autocomplete(ExecutionContext context) {

        // if we have more than 1 incomplete arg do nothing. user has to fix first argument
        if (context.getLevel().getArgsLeft().size() > 1) {
//            MsdmPlugin.highlight("too many unparsed args : " + context.getLevel().getArgsLeft());
            return new ArrayList<>();
        }
        ParsedArgument<?> invalidArg = context.getLevel().getFirstInvalidArgument();
        if (invalidArg != null) {
//            MsdmPlugin.highlight("invalid before last, dont autocomplete");
            // return hint saying to fix invalid arg
            return List.of("Invalid argument: " + invalidArg.getDeclaration().getName());
        }

        // autocomplete with child methods
        List<String> options = new ArrayList<>();

        boolean missingArg = (
            !context.getLevel().missingRequired().isEmpty() &&
            !context.getLevel().getArgsLeft().isEmpty()
        );

        // argument is possibly completable if there is an argument,
        // and it is not completed yet (no intermediate arg next)
        // and we haven't gone far (optional args)
        boolean possiblyHasCompletableArg = (
            !context.getLevel().getParsedRequired().isEmpty() &&
            context.getLevel().getArgsLeft().isEmpty() &&
            context.getLevel().getParsedOptional().isEmpty()
        );


        if (missingArg || possiblyHasCompletableArg) {
            CommandArgument<?> arg;

            // we are in process of completing arg AND we are missing args, generally this means empty string arg
            if (missingArg) {
                arg = context.getLevel().missingRequired().get(0);
            } else { // let's check if there are any further suggestions for this arg even though it already has value
                var declaredReqArgs = context.getLevel().getHandler().getDeclaredPositionalArgs().args;
                arg = declaredReqArgs.get(context.getLevel().getParsedRequired().size() - 1); // get last parsed arg
            }

            List<String> argOptions = arg.getOptions(context);

            if (!argOptions.isEmpty()) {
                options.addAll(argOptions);
            } else /*if (missingArg)*/ { // we only want to add placeholder arg in case there is no existing value
                options.add("<HINT:" + arg.getName() + ":" + arg.getTypeName() + "!>");
            }

            String toCompleteRequired;

            if (!context.getLevel().getArgsLeft().isEmpty()) {
                toCompleteRequired = context.getLevel().getArgsLeft().get(0);
            } else {
                toCompleteRequired = context.getLevel().getParsedRequired().getRaw(arg);
            }

            List<String> applicableRequiredOptions = filterApplicable(toCompleteRequired, options);

            if (missingArg || !applicableRequiredOptions.isEmpty()) {
                MsdmPlugin.highlight("return applicableRequiredOptions");
                return applicableRequiredOptions;
            }

        }

        // if required are in place we have every child command as option
        options.addAll(context.getLevel().getHandler().getChildren().keySet());

//        MsdmPlugin.highlight("completing level: " + context.getLevel().getHandler().getDescription());
//        context.getLevel().getHandler().getDeclaredOptionalArgs().args.forEach(arg -> {
//            MsdmPlugin.highlight("Declared optional arg on this level: " + arg.getName());
//        });

        // autocompletion for possible optional arguments
        List<String> optionalArgAutocompletes = new ArrayList<>();

        // add autocomplete for positional optional argument
        // note: possible bug. I take first optional that is missing, but if before named args were used,
        // indexes of missing optionals will not match definition indexes. will only work if parsing logic
        // works the same way

        // get last argument that has a value, it needs to be autocompleted.
        ParsedArgument<?> lastOptArg = context.getLevel().getParsedOptional().getLast();

        // if there are not parsed args after last parsed optional,
        // we no longer want to complete last parsed value, because
        // it is not last anymore.
        if (!context.getLevel().getArgsLeft().isEmpty()) {
            lastOptArg = null;
        }

//        MsdmPlugin.highlight("Parsed optionals: " + context.getLevel().getParsedOptional());

        boolean _completingPrefix = false;

        if (!context.getLevel().getArgsLeft().isEmpty()) {
            String firstUnparsedArg = context.getLevel().getArgsLeft().get(0);
//            MsdmPlugin.highlight("There is ignored unparsed arg: " + firstUnparsedArg);
            // we are entering -X prefix. Let's suggest all prefixes for arguments
            // that are not yet provided
            if (firstUnparsedArg.startsWith("-")) {
//                MsdmPlugin.highlight("Incomplete arg appears to be a prefix");
                var declaredArgs = context.getLevel().missingOptionals(false);
                optionalArgAutocompletes.addAll(declaredArgs.stream().map(arg -> "-" + arg.getName()).toList());
                _completingPrefix = true;
            }
        }
        if (lastOptArg != null && !_completingPrefix) {
//            MsdmPlugin.highlight(
//                    "lastArgWithValue needs autocomplete: " +
//                            lastOptArg.getDeclaration().getName() +
//                            " is positional: " + lastOptArg.isPositional()
//            );

            // add prefixes as autocomplete options except for the situations
            // when argument is being interpreted as positional value so far, meaning there
            // is no completed prefix yet.
            if (lastOptArg.isPositional()) {
                var declaredArgs = context.getLevel().missingOptionals(false);
                optionalArgAutocompletes.addAll(declaredArgs.stream().map(arg -> "-" + arg.getName()).toList());
                // also include the prefix of the last argument (the one we are looking at),
                // because we consider it not finished, even though it has *some* value
                optionalArgAutocompletes.add("-" + lastOptArg.getDeclaration().getName());
            }

            // value is null when -X prefix is used but no value is given yet
            // value is not null when started typing. Either way, since this is the last arg,
            // we consider it to not be completed.
            optionalArgAutocompletes.addAll(lastOptArg.getDeclaration().getOptions(context));
        }



//        for (CommandArgument<?> missingOptArg : context.getLevel().missingOptionals(true)) {
//
//            MsdmPlugin.highlight("check missing optional " + missingOptArg.getName() + " for options");
//
//            // there is -X prefix but no value yet
//            if (context.getLevel().getParsedOptional().isNull(missingOptArg.getName())) {
//
//                MsdmPlugin.highlight("Get options for prefixed arg " + missingOptArg.getName());
//                List<String> argOptions = missingOptArg.getOptions(context);
//                MsdmPlugin.highlight("Options to be used for prefixed arg " + missingOptArg.getName() + " : " + argOptions);
//                if (!argOptions.isEmpty()) {
//                    options.addAll(argOptions);
//                } else {
//                    options.add("<HINT:" + missingOptArg.getName() + "?>");
//                }
//
//                // we don't care about other possible arguments now that we have -X prefix,
//                // completing this "-key value" is the highest priority
//                // remove all other options and add autocomplete for this one
//                optionalArgAutocompletes.clear();
//                optionalArgAutocompletes.addAll(argOptions);
//                break;
//
//            // there is no value for this arg at all
//            } else if (!context.getLevel().getParsedOptional().containsNamedArg(missingOptArg.getName())) {
//                MsdmPlugin.highlight("add " + "-" + missingOptArg.getName());
//                optionalArgAutocompletes.add("-" + missingOptArg.getName());
//            }
//        }

        options.addAll(optionalArgAutocompletes);

        if (options.isEmpty()) {
            if (lastOptArg != null) {
                var decl = lastOptArg.getDeclaration();
                options.add("<HINT:" + decl.getName() + ":" + decl.getTypeName() + "?>");
            }
        }

//        MsdmPlugin.highlight("options are : " + options);


        List<String> suggestions = new ArrayList<>();

        // first try to complete with arg that was not recognised. This usually happens when
        // arg is partially written name of child command, and optional argument was not able to grab it,
        // because command does not have optionals at that level.
        // In case we are trying to autocomplete optional, we need to filter suggestions by text content of
        // last parsed optional argument. It would grab anything, even if not valid.
        // (in case of numbers or booleans where value can't be parsed, it still would end up in args left?)
        if (!context.getLevel().getArgsLeft().isEmpty()) {

            // we will be completing first piece that is not recognized by parser.
            // this will probably result in weird behaviour where in command such as /game invalid whatever
            // you will be receiving suggestions for word "invalid" instead of "whatever"
            String toComplete = context.getLevel().getArgsLeft().get(0);

            // find what of options we found actually fits
            suggestions = filterApplicable(toComplete, options);
//            MsdmPlugin.highlight("Filter applicable for '" + toComplete + "' (latest not recognized arg)");
        } else {
            if (lastOptArg != null) {
                Object value = lastOptArg.getRawValue();
//                MsdmPlugin.highlight("Filter applicable for '" + value + "' (latest parsed value)");
                // if null, new arg was not started yet and only prefix exists
                // '-X' -> value = null
                // '-X ' -> value = ""
                if (value != null) {
                    suggestions = filterApplicable(value.toString(), options);
                }
            }
        }

//        MsdmPlugin.highlight("return suggestions " + suggestions);
        return suggestions;
    }
}
