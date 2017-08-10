package me.dags.command.element;

import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Input;
import me.dags.command.element.function.Filter;
import me.dags.command.element.function.Options;
import me.dags.command.element.function.ValueParser;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class NodeElement extends ValueElement {

    private final String main;

    public NodeElement(String key, List<String> aliases) {
        super(key, Options.of(aliases), Filter.STARTS_WITH, ValueParser.node(aliases));
        this.main = aliases.get(0);
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        getParser().parse(input);
    }

    @Override
    public String toString() {
        return main;
    }
}
