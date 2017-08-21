package me.dags.command.command;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class Flags extends Context {

    public Flags() {
        super(new Object() {
            @Override
            public String toString() {
                return "flags";
            }
        });
    }

    @Override
    public <T> Optional<T> getSource(Class<T> type) {
        return Optional.empty();
    }
}
