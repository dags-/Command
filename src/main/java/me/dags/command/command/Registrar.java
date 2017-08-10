package me.dags.command.command;

import me.dags.command.annotation.processor.Processor;
import me.dags.command.element.ElementFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class Registrar<T extends Command<?>> {

    private final Processor processor = new Processor(this);
    private final Map<String, Entry> builders = new HashMap<>();

    private final ElementFactory elementFactory;
    private final CommandFactory<T> commandFactory;

    public Registrar(ElementFactory elementFactory, CommandFactory<T> commandFactory) {
        this.elementFactory = elementFactory;
        this.commandFactory = commandFactory;
    }

    public CommandFactory<T> getCommandFactory() {
        return commandFactory;
    }

    public ElementFactory getElementFactory() {
        return elementFactory;
    }

    public int register(Object source) {
        return processor.process(source);
    }

    public void register(Collection<String> aliases, CommandExecutor executor) {
        Entry builder = null;
        for (String alias : aliases) {
            builder = builders.get(alias);
            if (builder != null) {
                break;
            }
        }

        if (builder == null) {
            builder = new Entry();
        }

        for (String alias : aliases) {
            builders.put(alias, builder);
        }

        builder.aliases.addAll(aliases);
        builder.list.add(executor);
    }

    public Collection<T> build() {
        return builders.values().stream().distinct()
                .map(builder -> getCommandFactory().create(builder.aliases, builder.list))
                .collect(Collectors.toList());
    }

    public static <T extends Command<?>> Registrar<T> of(ElementFactory elementFactory, CommandFactory<T> commandFactory) {
        return new Registrar<>(elementFactory, commandFactory);
    }

    private static class Entry {

        private final List<CommandExecutor> list = new ArrayList<>();
        private final Set<String> aliases = new LinkedHashSet<>();
    }
}
