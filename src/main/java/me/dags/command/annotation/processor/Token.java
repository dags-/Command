package me.dags.command.annotation.processor;

import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Token {

    private final List<String> aliases;
    private final boolean node;

    Token(String alias, boolean node) {
        this.aliases = Collections.singletonList(alias);
        this.node = node;
    }

    Token(List<String> aliases, boolean node) {
        this.aliases = aliases;
        this.node = node;
    }

    public boolean isNode() {
        return node;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getAlias() {
        return aliases.get(0);
    }
}
