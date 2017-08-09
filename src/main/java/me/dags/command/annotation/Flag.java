package me.dags.command.annotation;

import java.lang.annotation.*;

/**
 * @author dags <dags@dags.me>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CommandFlags.class)
public @interface Flag {

    String value();

    Class<?> type() default boolean.class;
}
