package me.dags.command.element;

import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Input;
import me.dags.command.element.function.Filter;
import me.dags.command.element.function.Options;
import me.dags.command.element.function.ValueParser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class ValueElement implements Element {

    private static final Comparator<String> SORTER = (s1, s2) -> {
        if (s1.length() != s2.length()) {
            return Integer.compare(s1.length(), s2.length());
        }
        return s1.compareTo(s2);
    };

    private final String key;
    private final Filter filter;
    private final Options options;
    private final ValueParser<?> parser;

    ValueElement(String key, Options options, Filter filter, ValueParser<?> parser) {
        this.key = key;
        this.filter = filter;
        this.options = options;
        this.parser = parser;
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        if (!input.hasNext()) {
            return;
        }

        if (getOptions() == Options.EMPTY) {
            Object value = parser.parse(input);
            context.add(key, value);
            return;
        }

        int start = input.getPos();
        String next = input.next();
        List<String> matches = options.get().filter(s -> filter.test(s, next)).collect(Collectors.toList());
        List<Object> results = new ArrayList<>(matches.size());

        for (String match : matches) {
            input.setPos(start);
            Object val = parser.parse(input);

            if (next.equalsIgnoreCase(match)) {
                context.add(getKey(), val);
                return;
            }

            results.add(val);
        }

        for (Object val : results) {
            context.add(getKey(), val);
        }
    }

    @Override
    public void suggest(Input input, List<String> suggestions) {
        if (!input.hasNext()) {
            getOptions().get().sorted(SORTER).forEach(suggestions::add);
            return;
        }

        try {
            String next = input.next();
            if (options == Options.EMPTY) {
                return;
            }

            if (getOptions().get().anyMatch(s -> s.equalsIgnoreCase(next))) {
                return;
            }

            getOptions().get().filter(s -> filter.test(s, next)).sorted(SORTER).forEach(suggestions::add);
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean test(Input input) {
        if (input.hasNext()) {
            if (options == Options.EMPTY) {
                return true;
            }

            String next = input.peek();

            return getOptions().get().anyMatch(s -> getFilter().test(s, next));
        }
        return false;
    }

    public String getKey() {
        return key;
    }

    public ValueParser<?> getParser() {
        return parser;
    }

    public Options getOptions() {
        return options;
    }

    public Filter getFilter() {
        return filter;
    }
}
