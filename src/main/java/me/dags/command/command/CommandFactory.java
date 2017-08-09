package me.dags.command.command;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public interface CommandFactory<T extends Command<?>> {

    T create(Collection<String> aliases, Collection<CommandExecutor> executors);
}
