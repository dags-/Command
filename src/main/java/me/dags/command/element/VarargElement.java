package me.dags.command.element;

import java.util.List;
import java.util.Set;
import me.dags.command.annotation.processor.Param;
import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Input;

/**
 * @author dags <dags@dags.me>
 */
public class VarargElement implements Element {

    private final Element element;
    private final Set<String> flags;

    public VarargElement(Element element, Set<String> flags) {
        this.element = element;
        this.flags = flags;
    }

    @Override
    public String toString() {
        return "Vararg: [" + element.toString() + "]";
    }

    @Override
    public int getPriority() {
        return Param.Type.VARARG.priority();
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        while (input.hasNext()) {
            if (flags.contains(input.peek())) {
                break;
            }
            element.parse(input, context);
        }
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        while (input.hasNext()) {
            element.suggest(input, context, suggestions);
        }
    }
}
