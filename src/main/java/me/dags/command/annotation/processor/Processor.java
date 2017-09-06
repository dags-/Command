package me.dags.command.annotation.processor;

import com.google.common.collect.ImmutableMap;
import me.dags.command.annotation.*;
import me.dags.command.command.CommandExecutor;
import me.dags.command.command.Registrar;
import me.dags.command.element.Element;
import me.dags.command.element.ElementFactory;
import me.dags.command.element.NodeElement;
import me.dags.command.element.function.Filter;
import me.dags.command.element.function.Options;
import me.dags.command.element.function.ValueParser;
import me.dags.command.utils.IDGenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Processor {

    private final Registrar<?> registrar;

    public Processor(Registrar<?> registrar) {
        this.registrar = registrar;
    }

    public int process(Object o) {
        Class<?> c = o.getClass();
        int count = 0;
        do {
            for (Method method : c.getMethods()) {
                Command command = method.getAnnotation(Command.class);
                if (command == null) {
                    continue;
                }

                LinkedList<Token> tokens = new Tokenizer(command.value()).parse();
                if (tokens.size() > 0) {
                    Token root = tokens.pollFirst();
                    CommandExecutor executor = processMethod(o, method, tokens);
                    registrar.register(root.getAliases(), executor);
                    count++;
                }
            }
            c = c.getSuperclass();
        } while (c != null && c != Object.class);
        return count;
    }

    private CommandExecutor processMethod(Object object, Method method, LinkedList<Token> tokens) {
        Usage usage = getUsage(method, tokens);
        Permission permission = getPermission(method, tokens);
        Description description = getDescription(method);

        List<Param> params = new ArrayList<>();
        List<Element> elements = new ArrayList<>();
        processParameters(method, tokens, params, elements);

        return CommandExecutor.builder()
                .object(object)
                .method(method)
                .params(params)
                .elements(elements)
                .usage(usage)
                .permission(permission)
                .description(description)
                .build();
    }

    private void processParameters(Method m, LinkedList<Token> tokens, List<Param> params, List<Element> elements) {
        Map<String, Element> flags = buildFlags(m.getAnnotationsByType(Flag.class));

        IDGenerator generator = new IDGenerator();
        Parameter[] parameters = m.getParameters();

        for (Parameter parameter : parameters) {
            Param param = Param.of(generator, parameter);
            params.add(param);
        }

        for (int p = 0; p < params.size() || !tokens.isEmpty();) {
            if (!tokens.isEmpty()) {
                Token token = tokens.pollFirst();

                if (token.isNode()) {
                    String id = generator.getId(Object.class);
                    Element element = new NodeElement(id, token.getAliases());
                    elements.add(element);
                    continue;
                }
            }

            if (p < params.size()) {
                Param param;
                do {
                    param = params.get(p++);
                } while (p < params.size() && param.getParamType() == Param.Type.SOURCE);

                if (param.getParamType() != Param.Type.SOURCE) {
                    Element element = registrar.getElementFactory().create(param, flags);
                    elements.add(element);
                }
            }
        }
    }

    private Map<String, Element> buildFlags(Flag[] flags) {
        if (flags == null || flags.length == 0) {
            return Collections.emptyMap();
        }

        ElementFactory factory = registrar.getElementFactory();
        ImmutableMap.Builder<String, Element> builder = ImmutableMap.builder();

        for (Flag flag : flags) {
            String id = flag.value();
            String key = "-" + id;
            Class<?> type = flag.type();

            if (type == boolean.class || type == Boolean.class) {
                builder.put(key, Element.EMPTY);
            } else {
                ValueParser<?> parser = factory.getParser(type);
                Options options = factory.getOptions(type);
                Filter filter = factory.getFilter(type);
                builder.put(key, factory.createValueElement(id, 0, type, options, filter, parser));
            }
        }

        return builder.build();
    }

    private static Usage getUsage(Method method, List<Token> tokens) {
        Usage usage = method.getAnnotation(Usage.class);
        if (usage != null) {
            return usage;
        }

        Flag[] flags = method.getAnnotationsByType(Flag.class);

        StringBuilder flagBuilder = new StringBuilder();
        if (flags != null) {
            for (Flag flag : flags) {
                if (flagBuilder.length() > 0) {
                    flagBuilder.append(" | ");
                }

                flagBuilder.append("-").append(flag.value());

                if (flag.type() != boolean.class && flag.type() != Boolean.class) {
                    flagBuilder.append(" <").append(flag.type().getSimpleName()).append(">");
                }
            }
        }

        if (flagBuilder.length() > 0) {
            flagBuilder.insert(0, " (").append(")");
        }

        final String value = join(tokens, " ", "<", ">") + flagBuilder.toString();

        return new Usage() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Usage.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    private static Permission getPermission(Method method, List<Token> tokens) {
        Permission permission = method.getAnnotation(Permission.class);

        final String node;
        final Role role = permission == null ? EMPTY_ROLE : permission.role();

        if (permission == null) {
            node = "";
        } else if (permission.value().isEmpty()) {
            node = join(tokens, ".", "", "");
        } else {
            node = permission.value();
        }

        return new Permission() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Permission.class;
            }

            @Override
            public String value() {
                return node;
            }

            @Override
            public Role role() {
                return role;
            }
        };
    }

    private static Description getDescription(Method method) {
        Description description = method.getAnnotation(Description.class);
        return description == null ? EMPTY_DESCRIPTION : description;
    }

    private static final Role EMPTY_ROLE = new Role() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Role.class;
        }

        @Override
        public String value() {
            return "";
        }

        @Override
        public boolean permit() {
            return false;
        }
    };

    private static final Description EMPTY_DESCRIPTION = new Description() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Description.class;
        }

        @Override
        public String value() {
            return "";
        }
    };

    private static String join(List<Token> tokens, String separator, String startVal, String endVal) {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            if (builder.length() > 0) {
                builder.append(separator);
            }

            if (token.isNode()) {
                builder.append(token.getAlias());
            } else {
                builder.append(startVal).append(token.getAlias()).append(endVal);
            }
        }
        return builder.toString();
    }
}
