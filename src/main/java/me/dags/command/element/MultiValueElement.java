package me.dags.command.element;

import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Input;
import me.dags.command.element.function.Filter;
import me.dags.command.element.function.Options;
import me.dags.command.element.function.ValueParser;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class MultiValueElement extends ValueElement {

    public MultiValueElement(String key, int priority, Options options, Filter filter, ValueParser<?> parser) {
        super(key, priority, options, filter, parser);
    }

    @Override
    public String toString() {
        return "Value: [" + getKey() + "]";
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        if (getOptions() == Options.EMPTY) {
            Object o = getParser().parse(input);
            context.add(getKey(), o);
            return;
        }

        String next = input.next();
        String upper = next.toUpperCase();
        List<String> matches = getOptions().get().filter(s -> getFilter().test(s.toUpperCase(), upper)).collect(Collectors.toList());
        List<Object> results = new LinkedList<>();
        LinkedList<CommandException> exceptions = new LinkedList<>();

        for (String match : matches) {
            try {
                Object value = getParser().parse(match);
                results.add(value);
            } catch (CommandException e) {
                exceptions.add(e);
            }
        }

        if (!results.isEmpty()) {
            for (Object o : results) {
                context.add(getKey(), o);
            }
        } else if (!exceptions.isEmpty()) {
            throw exceptions.getLast();
        } else {
            throw new CommandException("Could not parse input '%s'", next);
        }
    }
}
