package me.dags.command.element;

import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Input;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class MultiValueElement implements Element {

    private final Element element;

    public MultiValueElement(Element element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return "Value: [" + element + "]";
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        List<String> suggestions = new LinkedList<>();
        LinkedList<CommandException> exceptions = new LinkedList<>();

        suggest(input, context, suggestions);

        boolean success = false;
        for (String suggestion : suggestions) {
            try {
                element.parse(input.replace(suggestion), context);
                success = true;
            } catch (CommandException e) {
                exceptions.add(e);
            }
        }

        if (!success && !exceptions.isEmpty()) {
            throw exceptions.getLast();
        }
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        element.suggest(input, context, suggestions);
    }
}
