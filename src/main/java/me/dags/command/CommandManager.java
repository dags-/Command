package me.dags.command;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import me.dags.command.command.Command;
import me.dags.command.command.CommandFactory;
import me.dags.command.command.Registrar;
import me.dags.command.element.ElementFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dags <dags@dags.me>
 */
public abstract class CommandManager<T extends Command<?>> {

    private final Object owner;
    private Registrar<T> registrar;

    public CommandManager(Builder<T> builder) {
        this.owner = builder.owner;
        this.registrar = Registrar.of(builder.elementFactory, builder.commandFactory);

    }

    public Object getOwner() {
        checkAccess();
        return owner;
    }

    public CommandManager<T> registerPackage(Class<?> child) {
        checkAccess();
        return registerPackage(true, child);
    }

    public CommandManager<T> registerPackage(boolean recursive, Class<?> child) {
        checkAccess();
        return registerPackage(recursive, child.getPackage().getName());
    }

    public CommandManager<T> registerPackage(String... path) {
        checkAccess();
        return registerPackage(true, path);
    }

    public CommandManager<T> registerPackage(boolean recurse, String... path) {
        checkAccess();
        info("Scanning package %s for commands...", Arrays.toString(path));
        ScanResult result = new FastClasspathScanner(path).disableRecursiveScanning(!recurse).scan();
        List<String> matches = result.getNamesOfAllClasses();
        info("Discovered %s Command classes in package %s", matches.size(), path);
        for (String name : matches) {
            try {
                Class<?> clazz = Class.forName(name);
                register(clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public CommandManager<T> register(Class<?> c) {
        checkAccess();
        try {
            Object o = c.newInstance();
            register(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public CommandManager<T> register(Object o) {
        checkAccess();
        int count = registrar.register(o);
        info("Registered %s command(s) from %s", count, o.getClass());
        return this;
    }

    public void submit() {
        checkAccess();
        Collection<T> commands = registrar.build();
        for (T t : commands) {
            submit(owner, t);
        }
    }

    private void checkAccess() {
        if (registrar == null) {
            throw new IllegalStateException("Attempted to access Registrar after it has been disposed!");
        }
    }

    protected abstract void submit(Object owner, T command);

    protected void info(String message, Object... args) {
        Logger.getLogger("CommandBus").info(String.format(message, args));
    }

    protected void warn(String message, Object... args) {
        Logger.getLogger("CommandBus").log(Level.WARNING, String.format(message, args));
    }

    public static <T extends Command<?>> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T extends Command<?>> {

        private Object owner;
        private ElementFactory elementFactory = ElementFactory.create();
        private CommandFactory<T> commandFactory;

        public Builder<T> owner(Object owner) {
            this.owner = owner;
            return this;
        }

        public Builder<T> elements(ElementFactory factory) {
            this.elementFactory = factory;
            return this;
        }

        public Builder<T> commands(CommandFactory<T> factory) {
            this.commandFactory = factory;
            return this;
        }

        public <V extends CommandManager<T>> V build(Function<Builder<T>, V> function) {
            return function.apply(this);
        }
    }
}
