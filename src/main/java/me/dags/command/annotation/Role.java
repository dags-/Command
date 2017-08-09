package me.dags.command.annotation;

/**
 * @author dags <dags@dags.me>
 */
public @interface Role {

    String value();

    boolean permit() default true;
}
