package me.dags.command.element;

import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Input;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public interface Element {

    void parse(Input input, Context context) throws CommandException;

    void suggest(Input input, List<String> suggestions);

    boolean test(Input input);

    Element EMPTY = new Element() {
        @Override
        public void parse(Input input, Context context) throws CommandException {}

        @Override
        public void suggest(Input input, List<String> suggestions) {}

        @Override
        public boolean test(Input input) {
            return false;
        }
    };
}
