package me.dags.command.element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.dags.command.annotation.processor.Param;
import me.dags.command.element.function.Filter;
import me.dags.command.element.function.Options;
import me.dags.command.element.function.ValueParser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author dags <dags@dags.me>
 */
public class ElementFactory {

    private final Map<Class<?>, ValueParser<?>> parsers;
    private final Map<Class<?>, Options> options;
    private final Map<Class<?>, Filter> filters;

    private final Options defaultOptions = Options.EMPTY;
    private final Filter defaultFilter = Filter.STARTS_WITH;
    private final ValueParser<?> defaultParser = ValueParser.EMPTY;

    protected ElementFactory(Builder builder) {
        this.options = ImmutableMap.copyOf(builder.options);
        this.filters = ImmutableMap.copyOf(builder.filters);
        this.parsers = ImmutableMap.<Class<?>, ValueParser<?>>builder()
                .putAll(builder.parsers)
                .putAll(ValueParser.DEFAULTS)
                .build();
    }

    public Element create(Param param, Map<String, Element> flags) {
        ValueParser<?> parser = getParser(param);
        Options options = getOptions(param);
        Filter filter = getFilter(param);
        int priority = param.getParamType().priority();

        if (parser == null) {
            throw new UnsupportedOperationException("Invalid element type " + param.getType());
        }

        if (param.getParamType() == Param.Type.VARARG) {
            return createVarargElement(param.getId(), priority, param.getType(), options, filter, parser, flags);
        }

        if (param.getParamType() == Param.Type.FLAG) {
            return createFlagElement(param.getId(), param.getType(), options, filter, parser, flags);
        }

        return createValueElement(param.getId(), priority, param.getType(), options, filter, parser);
    }

    public final Element createValueElement(String id, Class<?> type, Options options, Filter filter, ValueParser parser) {
        return createValueElement(id, Element.PRIORITY, type, options, filter, parser);
    }

    public final Element createVarargElement(String id, Class<?> type, Options options, Filter filter, ValueParser parser, Map<String, Element> flags) {
        return createVarargElement(id, Param.Type.VARARG.priority(), type, options, filter, parser, flags);
    }

    public final Element createFlagElement(String id, Class<?> type, Options options, Filter filter, ValueParser parser, Map<String, Element> flags) {
        return createFlagElement(id, Param.Type.FLAG.priority(), type, options, filter, parser, flags);
    }

    public Element createValueElement(String id, int priority, Class<?> type, Options options, Filter filter, ValueParser parser) {
        return new ValueElement(id, priority, options, filter, parser);
    }

    public Element createVarargElement(String id, int priority, Class<?> type, Options options, Filter filter, ValueParser parser, Map<String, Element> flags) {
        return new VarargElement(createValueElement(id, priority, type, options, filter, parser), flags.keySet());
    }

    public Element createFlagElement(String id, int priority, Class<?> type, Options options, Filter filter, ValueParser parser, Map<String, Element> flags) {
        return new FlagElement(id, flags);
    }

    public Options getOptions(Param param) {
        return getOptions(param.getType());
    }

    public Filter getFilter(Param param) {
        return getFilter(param.getType());
    }

    public ValueParser<?> getParser(Param param) {
        if (param.getParamType() == Param.Type.JOIN) {
            return ValueParser.joinedString(" ");
        }
        return getParser(param.getType());
    }

    public Options getOptions(Class<?> type) {
        if (Enum.class.isAssignableFrom(type)) {
            Object[] values = type.getEnumConstants();
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (Object value : values) {
                builder.add(value.toString());
            }
            return Options.of(builder.build());
        }

        return get(type, options, defaultOptions);
    }

    public Filter getFilter(Class<?> type) {
        return get(type, filters, defaultFilter);
    }

    @SuppressWarnings("unchecked")
    public ValueParser<?> getParser(Class<?> type) {
        if (Enum.class.isAssignableFrom(type)) {
            return ValueParser.enumParser((Class<? extends Enum>) type);
        }

        return get(type, parsers, defaultParser);
    }

    private <T> T get(Class<?> type, Map<Class<?>, T> map, T defaultVal) {
        while (type != null && type != Object.class) {
            T t = map.get(type);
            if (t != null) {
                return t;
            }
            type = type.getSuperclass();
        }
        return defaultVal;
    }

    public static ElementFactory create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Class<?>, ValueParser<?>> parsers = new HashMap<>();
        private final Map<Class<?>, Options> options = new HashMap<Class<?>, Options>(){{
            put(boolean.class, Options.of("false", "true"));
            put(Boolean.class, Options.of("false", "true"));
        }};
        private final Map<Class<?>, Filter> filters = new HashMap<>();

        public <T> Builder parser(Class<T> type, ValueParser<T> parser) {
            parsers.put(type, parser);
            return this;
        }

        public <T> Builder options(Class<T> type, Options options) {
            this.options.put(type, options);
            return this;
        }

        public <T> Builder filter(Class<T> type, Filter filter) {
            this.filters.put(type, filter);
            return this;
        }

        public ElementFactory build() {
            return new ElementFactory(this);
        }

        public ElementFactory build(Function<Builder, ElementFactory> func) {
            return func.apply(this);
        }
    }
}
