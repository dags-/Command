package me.dags.command.command;

import com.google.common.collect.ImmutableList;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Command<T> {

    private static final int SUGGESTION_LIMIT = 20;

    private final List<CommandExecutor> executors;
    private final List<String> aliases;

    public Command(Collection<String> aliases, Collection<CommandExecutor> executors) {
        this.executors = ImmutableList.copyOf(executors);
        this.aliases = ImmutableList.copyOf(aliases);
    }

    public void processArguments(T source, String arguments) throws CommandException {
        Input input = new Input(arguments);
        List<CommandException> exceptions = new ArrayList<>(executors.size());

        for (CommandExecutor executor : executors) {
            String permission = executor.getPermission().value();
            if (testPermission(source, permission)) {
                Context context;

                try {
                    context = executor.parse(source, input);
                } catch (CommandException e) {
                    exceptions.add(e);
                    continue;
                }

                try {
                    executor.invoke(context);
                    return;
                } catch (CommandException e) {
                    exceptions.add(e);
                }
            } else {
                exceptions.add(new CommandException("Requires the permission %s", permission));
            }
        }

        if (!exceptions.isEmpty()) {
            Collections.sort(exceptions);
            int last = exceptions.size() - 1;
            throw exceptions.get(last);
        }
    }

    public List<String> suggestCommand(T source, String rawInput) {
        Input input = new Input(rawInput);
        List<String> suggestions = new LinkedList<>();

        if (!input.hasNext()) {
            for (CommandExecutor executor : executors) {
                if (testPermission(source, executor.getPermission().value())) {
                    executor.getFirstSuggestion(source, input, suggestions);
                }
            }
            Collections.sort(suggestions);
        } else {
            for (CommandExecutor executor : executors) {
                if (testPermission(source, executor.getPermission().value())) {
                    executor.getSuggestions(source, input, suggestions);
                }
            }
            suggestions.sort(Comparator.comparing(String::length));
        }

        List<String> results = new ArrayList<>(SUGGESTION_LIMIT);
        for (String suggestion : suggestions) {
            if (!results.contains(suggestion)) {
                results.add(suggestion);
                if (results.size() >= SUGGESTION_LIMIT) {
                    break;
                }
            }
        }

        return results;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public List<CommandExecutor> getExecutors() {
        return executors;
    }

    public boolean testPermission(T source, String permission) {
        return true;
    }
}
