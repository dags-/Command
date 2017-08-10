package me.dags.command.command;

import me.dags.command.annotation.Description;
import me.dags.command.annotation.Permission;
import me.dags.command.annotation.Usage;
import me.dags.command.annotation.processor.Param;
import me.dags.command.element.Element;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class CommandExecutor implements Comparable<CommandExecutor> {

    private final Object object;
    private final Method method;
    private final List<Param> params;
    private final List<Element> elements;

    private final Usage usage;
    private final Permission permission;
    private final Description description;

    private CommandExecutor(Builder builder) {
        object = builder.object;
        method = builder.method;
        params = builder.params;
        elements = builder.elements;
        usage = builder.usage;
        permission = builder.permission;
        description = builder.description;
    }

    public Context parse(Object source, Input input) throws CommandException {
        input.reset();

        Context context = new Context();
        for (Param param : params) {
            if (param.getParamType() == Param.Type.SOURCE) {
                context.add(param.getId(), source);
            }
        }

        int priority = 0;
        for (Element element : elements) {
            try {
                element.parse(input, context);
            } catch (CommandException e) {
                throw e.priority(priority);
            }

            priority++;
        }

        if (input.hasNext()) {
            throw new CommandException("Too many args provided: '%s'", input.getRawInput()).priority(priority).usage(getUsage().value());
        }

        return context;
    }

    public void getSuggestions(Object source, Input input, List<String> suggestions) {
        input.reset();
        for (Element element : elements) {
            if (!input.hasNext()) {
                break;
            }

            if (!element.test(input)) {
                return;
            }

            element.suggest(input, suggestions);
        }
    }

    public void invoke(Context context) throws CommandException {
        Object[] args = new Object[params.size()];

        for (int i = 0; i < params.size(); i++) {
            Param param = params.get(i);
            Object val = context.get(param, param.getType());

            if (val == null) {
                throw new CommandException("Parameter %s missing from Context", param.getId()).usage(getUsage().value());
            }

            args[i] = val;
        }

        try {
            method.invoke(object, args);
        } catch (Throwable t) {
            throw new CommandException(t.getMessage());
        }
    }

    public Usage getUsage() {
        return usage;
    }

    public Permission getPermission() {
        return permission;
    }

    public Description getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getUsage().value();
    }

    @Override
    public int compareTo(CommandExecutor executor) {
        int result = Integer.compare(elements.size(), executor.elements.size());
        return result != 0 ? result : getUsage().value().compareTo(executor.getUsage().value());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Object object;
        private Method method;
        private List<Param> params;
        private List<Element> elements;
        private Usage usage;
        private Permission permission;
        private Description description;

        private Builder() {}

        public Builder object(Object object) {
            this.object = object;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder params(List<Param> params) {
            this.params = params;
            return this;
        }

        public Builder elements(List<Element> elements) {
            this.elements = elements;
            return this;
        }

        public Builder usage(Usage usage) {
            this.usage = usage;
            return this;
        }

        public Builder permission(Permission permission) {
            this.permission = permission;
            return this;
        }

        public Builder description(Description description) {
            this.description = description;
            return this;
        }

        public CommandExecutor build() {
            return new CommandExecutor(this);
        }
    }
}
