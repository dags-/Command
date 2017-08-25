package me.dags.command.command;

import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Input {

    private final String rawInput;
    private final List<String> args;
    private int pos = -1;

    public Input(String input) {
        this.args = ImmutableList.copyOf(parse(input, true));
        this.rawInput = input;
    }

    public boolean hasNext() {
        return pos + 1 < args.size();
    }

    private String current() {
        return args.get(Math.min(args.size() - 1, Math.max(0, pos)));
    }

    public String peek() {
        return args.get(pos + 1);
    }

    public String next() throws CommandException {
        if (!hasNext()) {
            throw new CommandException("Not enough args provided: '%s'", rawInput);
        }
        return args.get(++pos);
    }

    public Input slice(int start) {
        return slice(start, rawInput.length());
    }

    public Input slice(int start, int end) {
        String s = rawInput.substring(start, end);
        return new Input(s);
    }

    public Input replace(String replacement) {
        String find = hasNext() ? peek() : current();
        int pos = getCursor();
        String start = rawInput.substring(0, pos);
        String end = rawInput.substring(pos).replace(find, replacement);
        return new Input(start + end);
    }

    public int getPos() {
        return pos;
    }

    public int getCursor() {
        if (hasNext()) {
            String next = peek();
            int index = 0;

            for (int i = 0; i < pos + 1; i++) {
                index += args.get(i).length();
                if (i > 0) {
                    index++; // space
                }
            }

            return rawInput.indexOf(next, index);
        }

        return 0;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Input last() {
        pos = args.size() - 2;
        return this;
    }

    public Input reset() {
        pos = -1;
        return this;
    }

    public String getRawInput() {
        return rawInput;
    }

    private static List<String> parse(String args, boolean quotes) {
        List<String> results = new LinkedList<>();

        outer:
        for (int i = 0, end = args.length(); i < end; i++) {
            // skip whitespace
            while (i < end && args.charAt(i) == ' ') {
                if (++i == end) {
                    break outer;
                }
            }

            char c = args.charAt(i);

            // if quoted read until next ' or "
            char stop = (quotes && (c == '\'' || c == '"')) ? c : ' ';

            // if quoted, move forward one position to start reading the inside string
            int from = stop == ' ' ? i : i + 1;
            i = from;

            while (i < end && args.charAt(i) != stop) {
                i++;
            }

            String s = args.substring(from, i);
            results.add(s);
        }

        if (args.endsWith(" ")) {
            results.add("");
        }

        return results;
    }
}
