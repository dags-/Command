package me.dags.command.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Command<T> {

    private final List<CommandExecutor> executors;
    private final List<String> aliases;

    public Command(Collection<String> aliases, Collection<CommandExecutor> executors) {
        this.executors = new ArrayList<>(executors);
        this.aliases = new ArrayList<>(aliases);
    }

    public void processCommand(T source, String rawInput) throws CommandException {
        Input input = new Input(rawInput);
        CommandException last = null;

        for (CommandExecutor executor : executors) {
            String permission = executor.getPermission().value();
            if (testPermission(source, permission)) {
                Context context;

                try {
                    context = executor.parse(source, input);
                } catch (CommandException e) {
                    last = e;
                    continue;
                }

                try {
                    executor.invoke(context);
                    return;
                } catch (CommandException e) {
                    last = e;
                }

            } else {
                last = new CommandException("Requires the permission %s", permission);
            }
        }

        if (last != null) {
            throw last;
        }
    }

    public List<String> suggestCommand(T source, String rawInput) {
        Input input = new Input(rawInput);
        List<String> suggestions = new LinkedList<>();
        for (CommandExecutor executor : executors) {
            if (testPermission(source, executor.getPermission().value())) {
                int pos = input.getPos();
                executor.getSuggestions(source, input, suggestions);
                input.setPos(pos);
            }
        }
        return suggestions;
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
