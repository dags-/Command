package me.dags.command.element.function;

import com.google.common.collect.ImmutableMap;
import me.dags.command.command.CommandException;
import me.dags.command.command.Input;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
@FunctionalInterface
public interface ValueParser<T> {

    T parse(Input input) throws CommandException;

    ValueParser<?> EMPTY = input -> null;

    Map<Class<?>, ValueParser<?>> DEFAULTS = ImmutableMap.<Class<?>, ValueParser<?>>builder()
            .put(byte.class, ValueParser.of(Byte::parseByte))
            .put(Byte.class, ValueParser.of(Byte::parseByte))
            .put(boolean.class, ValueParser.of(Boolean::parseBoolean))
            .put(Boolean.class, ValueParser.of(Boolean::parseBoolean))
            .put(double.class, ValueParser.of(Double::parseDouble))
            .put(Double.class, ValueParser.of(Double::parseDouble))
            .put(float.class, ValueParser.of(Float::parseFloat))
            .put(Float.class, ValueParser.of(Float::parseFloat))
            .put(int.class, ValueParser.of(Integer::parseInt))
            .put(Integer.class, ValueParser.of(Integer::parseInt))
            .put(long.class, ValueParser.of(Long::parseLong))
            .put(Long.class, ValueParser.of(Long::parseLong))
            .put(short.class, ValueParser.of(Short::parseShort))
            .put(Short.class, ValueParser.of(Short::parseShort))
            .put(String.class, Input::next)
            .build();

    static <T> ValueParser<T> of(Function<String, T> function) {
        return input -> function.apply(input.next());
    }

    static ValueParser<String> joinedString(String separator) {
        return input -> {
            StringBuilder sb = new StringBuilder();
            while (input.hasNext()) {
                sb.append(sb.length() > 0 ? separator : "").append(input.next());
            }
            return sb.toString();
        };
    }

    static ValueParser<Object> node(Collection<String> options) {
        return input -> {
            String next = input.next();
            for (String option : options) {
                if (option.equalsIgnoreCase(next)) {
                    return null;
                }
            }
            throw new CommandException("Invalid input %s, expected: %s", next, options);
        };
    }
}
