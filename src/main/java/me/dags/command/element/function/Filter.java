package me.dags.command.element.function;

import java.util.function.BiPredicate;

/**
 * @author dags <dags@dags.me>
 */
@FunctionalInterface
public interface Filter extends BiPredicate<String, String> {

    Filter STARTS_WITH = String::startsWith;

    Filter ENDS_WITH = String::endsWith;

    Filter CONTAINS = String::contains;

    Filter EQUALS = String::equals;

    Filter EQUALS_IGNORE_CASE = String::equalsIgnoreCase;
}
