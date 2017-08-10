package me.dags.command.command;

/**
 * @author dags <dags@dags.me>
 */
public class CommandException extends Exception implements Comparable<CommandException> {

    private int priority;
    private String usage;

    public CommandException(String message, Object... args) {
        super(String.format(message, args));
        this.priority = 0;
    }

    public CommandException usage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandException priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public String getMessage() {
        if (usage == null) {
            return super.getMessage();
        }
        return String.format("%s. Expected: %s", super.getMessage(), usage);
    }

    @Override
    public int compareTo(CommandException e) {
        return Integer.compare(priority, e.priority);
    }
}
