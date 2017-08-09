package me.dags.command.annotation.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Tokenizer {

    private final String input;
    private int pos = -1;

    public Tokenizer(String input) {
        this.input = input;
    }

    public List<Token> parse() {
        List<Token> elements = new LinkedList<>();
        while (hasNext()) {
            elements.add(nextToken());
        }
        return new ArrayList<>(elements);
    }

    private boolean hasNext() {
        return pos + 1 < input.length();
    }

    private char next() {
        return input.charAt(++pos);
    }

    private char peek() {
        return input.charAt(1 + pos);
    }

    private void skipSpace() {
        while (hasNext() && peek() == ' ') {
            next();
        }
    }

    private Token nextToken() {
        skipSpace();

        int start = Math.max(pos + 1, 0);
        int end = start + 1;

        char c = next();
        char stop = ' ';

        boolean arg = c == '<';
        if (arg) {
            start = pos + 1;
            stop = '>';
            while (hasNext() && next() != stop) {
                end = pos + 1;
            }
            return new Token(input.substring(start, end), false);
        } else {
            List<String> list = new LinkedList<>();
            while (hasNext() && (c = next()) != stop) {
                end = pos + 1;
                if (c == '|') {
                    list.add(input.substring(start, pos));
                    start = end;
                }
            }
            list.add(input.substring(start, end));
            return new Token(list, true);
        }
    }
}
