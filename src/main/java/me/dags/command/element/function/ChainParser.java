package me.dags.command.element.function;

import me.dags.command.command.CommandException;
import me.dags.command.command.Input;

/**
 * @author dags <dags@dags.me>
 */
public interface ChainParser<D, T> {

    T map(Input input, D d) throws CommandException;
}
