package me.dags.command.element.function;

import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public interface ChainOptions<D> {

    Stream<String> get(D d);
}
