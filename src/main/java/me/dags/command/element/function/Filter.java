package me.dags.command.element.function;

import java.util.function.BiPredicate;

/**
 * @author dags <dags@dags.me>
 */
@FunctionalInterface
public interface Filter extends BiPredicate<String, String> {

    Filter STARTS_WITH = ignoreCase(String::startsWith);

    Filter ENDS_WITH = ignoreCase(String::endsWith);

    Filter CONTAINS = ignoreCase(String::contains);

    Filter EQUALS = ignoreCase(String::contentEquals);

    Filter EQUALS_IGNORE_CASE = String::equalsIgnoreCase;

    static Filter ignoreCase(BiPredicate<String, String> filter) {
        return (s1, s2) -> filter.test(s1.toUpperCase(), s2.toUpperCase());
    }
}
