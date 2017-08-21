package me.dags.command.element;

import me.dags.command.element.function.Filter;
import me.dags.command.element.function.Options;
import me.dags.command.element.function.ValueParser;

/**
 * @author dags <dags@dags.me>
 */
public interface ElementProvider {

    Element create(String id, int priority, Options options, Filter filter, ValueParser parser);
}
