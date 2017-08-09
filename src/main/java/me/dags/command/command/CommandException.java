package me.dags.command.command;

/**
 * @author dags <dags@dags.me>
 */
public class CommandException extends Exception {

    public CommandException(String message, Object... args) {
        super(String.format(message, args));
    }
}
