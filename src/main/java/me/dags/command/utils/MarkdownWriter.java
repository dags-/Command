package me.dags.command.utils;

import me.dags.command.command.CommandExecutor;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class MarkdownWriter implements Closeable {

    private final Appendable appendable;

    public MarkdownWriter(Appendable appendable) {
        this.appendable = appendable;
    }

    public MarkdownWriter writeHeaders() {
        write("| Command | Permission | Description |").newLine();
        write("| :------ | :--------- | :---------- |");
        return this;
    }

    public MarkdownWriter writeCommand(CommandExecutor executor) {
        String command = executor.getUsage().value();
        String permission = executor.getPermission().value();
        String description = executor.getDescription().value();
        newLine().write(String.format("| `%s` | `%s` | %s |", command, permission, description));
        return this;
    }

    private MarkdownWriter write(String string) {
        try {
            appendable.append(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private MarkdownWriter newLine() {
        write("\n");
        return this;
    }

    @Override
    public void close() throws IOException {
        if (appendable instanceof Flushable) {
            Flushable flushable = (Flushable) appendable;
            flushable.flush();
        }
        if (appendable instanceof Closeable) {
            Closeable closeable = (Closeable) appendable;
            closeable.close();
        }
    }
}
