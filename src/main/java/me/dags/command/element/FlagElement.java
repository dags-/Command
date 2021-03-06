package me.dags.command.element;

import java.util.List;
import java.util.Map;
import me.dags.command.annotation.processor.Param;
import me.dags.command.command.CommandException;
import me.dags.command.command.Context;
import me.dags.command.command.Flags;
import me.dags.command.command.Input;

/**
 * @author dags <dags@dags.me>
 */
public class FlagElement implements Element {

    private final Map<String, Element> flags;
    private final String id;

    public FlagElement(String id, Map<String, Element> flags) {
        this.flags = flags;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Flags: " + id;
    }

    @Override
    public int getPriority() {
        return Param.Type.FLAG.priority();
    }

    @Override
    public void parse(Input input, Context context) throws CommandException {
        input.reset();

        Flags flags = new Flags();
        CommandException delayed = null;

        while (input.hasNext()) {
            try {
                String flag = input.next();
                Element element = this.flags.get(flag);

                if (element != null) {
                    if (element != Element.EMPTY) {
                        element.parse(input, flags);
                    } else {
                        flags.add(flag.substring(1), true);
                    }
                }
            } catch (CommandException e) {
                delayed = e;
            }
        }

        context.add(id, flags);

        if (delayed != null) {
            throw delayed;
        }
    }

    @Override
    public void suggest(Input input, Context context, List<String> suggestions) {
        input.last().setPos(input.getPos() - 1);

        try {
            String flag = input.next();
            flag = flag.startsWith("-") ? flag : input.next();

            if (flag.startsWith("-")) {
                Element element = flags.getOrDefault(flag, Element.EMPTY);

                if (element != Element.EMPTY) {
                    element.suggest(input, context, suggestions);
                    return;
                }
            }

            String match = flag;
            flags.keySet().stream().filter(s -> s.startsWith(match)).sorted().forEach(suggestions::add);
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }
}
