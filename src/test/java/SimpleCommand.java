import me.dags.command.command.Command;
import me.dags.command.command.CommandException;
import me.dags.command.command.CommandExecutor;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public class SimpleCommand extends Command<Object> {

    public SimpleCommand(Collection<String> aliases, Collection<CommandExecutor> executors) {
        super(aliases, executors);
    }

    @Override
    public void processCommand(Object source, String args) throws CommandException {
        System.out.println(args);
        super.processCommand(source, args);
    }
}
