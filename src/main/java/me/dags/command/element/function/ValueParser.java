package me.dags.command.element.function;

import com.google.common.collect.ImmutableMap;
import me.dags.command.command.CommandException;
import me.dags.command.command.Input;

import java.util.Collection;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
@FunctionalInterface
public interface ValueParser<T> {

    T parse(String input) throws CommandException;

    default T parse(Input input) throws CommandException {
        return parse(input.next());
    }

    ValueParser<?> EMPTY = input -> null;

    Map<Class<?>, ValueParser<?>> DEFAULTS = ImmutableMap.<Class<?>, ValueParser<?>>builder()
            .put(byte.class, Byte::parseByte)
            .put(Byte.class, Byte::parseByte)
            .put(boolean.class, ValueParser.bool())
            .put(Boolean.class, ValueParser.bool())
            .put(double.class, Double::parseDouble)
            .put(Double.class, Double::parseDouble)
            .put(float.class, Float::parseFloat)
            .put(Float.class, Float::parseFloat)
            .put(int.class, Integer::parseInt)
            .put(Integer.class, Integer::parseInt)
            .put(long.class, Long::parseLong)
            .put(Long.class, Long::parseLong)
            .put(short.class, Short::parseShort)
            .put(Short.class, Short::parseShort)
            .put(String.class, s -> s)
            .build();

    static ValueParser<String> joinedString(String separator) {
        return new ValueParser<String>() {
            @Override
            public String parse(Input input) throws CommandException {
                StringBuilder sb = new StringBuilder();
                while (input.hasNext()) {
                    sb.append(sb.length() > 0 ? separator : "").append(input.next());
                }
                return sb.toString();
            }

            @Override
            public String parse(String input) throws CommandException {
                return input;
            }
        };
    }

    static ValueParser<Object> node(Collection<String> options) {
        return next -> {
            for (String option : options) {
                if (option.equalsIgnoreCase(next)) {
                    return null;
                }
            }
            throw new CommandException("Invalid arg '%s'", next);
        };
    }

    static ValueParser<Object> enumParser(Class<? extends Enum> c) {
        return input -> {
            try {
                input = input.toUpperCase();
                return Enum.valueOf(c, input);
            } catch (IllegalArgumentException e) {
                throw new CommandException("Invalid enum value '%s'", input);
            }
        };
    }

    static ValueParser<Boolean> bool() {
        return input -> {
            if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                return Boolean.valueOf(input);
            }
            throw new CommandException("Invalid boolean value '%s", input);
        };
    }
}
