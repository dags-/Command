import me.dags.command.command.Command;
import me.dags.command.command.CommandExecutor;

import java.util.Collection;

/**
 * @author dags <dags@dags.me>
 */
public class SimpleCommand extends Command<Object> {

    public SimpleCommand(Collection<String> aliases, Collection<CommandExecutor> executors) {
        super(aliases, executors);
    }
}
