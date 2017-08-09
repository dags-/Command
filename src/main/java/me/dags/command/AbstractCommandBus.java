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
public abstract class AbstractCommandBus<T extends Command<?>> {

    private final Object owner;
    private Registrar<T> registrar;

    public AbstractCommandBus(Builder<T> builder) {
        this.owner = builder.owner;
        this.registrar = Registrar.of(builder.elementFactory, builder.commandFactory);
    }

    public AbstractCommandBus registerPackage(Class<?> child) {
        checkAccess();
        return registerPackage(true, child);
    }

    public AbstractCommandBus registerPackage(boolean recursive, Class<?> child) {
        checkAccess();
        return registerPackage(recursive, child.getPackage().getName());
    }

    public AbstractCommandBus registerPackage(String... path) {
        checkAccess();
        return registerPackage(true, path);
    }

    public AbstractCommandBus registerPackage(boolean recurse, String... path) {
        checkAccess();

        info("Scanning package {} for commands...", Arrays.toString(path));
        ScanResult result = new FastClasspathScanner(path).disableRecursiveScanning(!recurse).scan();
        List<String> matches = result.getNamesOfAllClasses();
        info("Discovered {} Command classes in package {}", matches.size(), path);
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

    public AbstractCommandBus register(Object o) {
        checkAccess();
        if (registrar != null) {
            registrar.register(o);
        }
        return this;
    }

    public AbstractCommandBus register(Class<?> c) {
        checkAccess();
        try {
            Object o = c.newInstance();
            register(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void submit() {
        checkAccess();
        if (registrar != null) {
            Collection<T> commands = registrar.build();
            for (T t : commands) {
                submit(owner, t);
            }
        }
    }

    protected Registrar<T> getRegistrar() {
        checkAccess();
        return registrar;
    }

    protected abstract void submit(Object owner, T command);

    private void checkAccess() {
        if (registrar == null) {
            throw new IllegalStateException("Attempted to access Registrar after it has been disposed!");
        }
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

        public <V extends AbstractCommandBus<T>> V build(Function<Builder<T>, V> function) {
            return function.apply(this);
        }
    }

    protected void info(String message, Object... args) {
        Logger.getLogger("CommandBus").info(String.format(message, args));
    }

    protected void warn(String message, Object... args) {
        Logger.getLogger("CommandBus").log(Level.WARNING, String.format(message, args));
    }
}
