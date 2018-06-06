import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import me.dags.command.CommandManager;
import me.dags.command.command.Command;
import me.dags.command.utils.MarkdownWriter;

/**
 * @author dags <dags@dags.me>
 */
public class SimpleCommandBus extends CommandManager<SimpleCommand> {

    private final Map<String, SimpleCommand> commands = new HashMap<>();

    public SimpleCommandBus(Builder<SimpleCommand> builder) {
        super(builder);
    }

    @Override
    protected void submit(Object owner, SimpleCommand command) {
        for (String key : command.getAliases()) {
            commands.put(key, command);
        }

        long unique = commands.values().stream().distinct().count();
        long executors = commands.values().stream().map(Command::getExecutors).flatMap(List::stream).distinct().count();
        info("%s registered %s command(s) and %s executor(s)", owner, unique, executors);
    }

    public String getDocumentation() {
        StringBuilder sb = new StringBuilder();
        writeDocumentation(sb);
        return sb.toString();
    }

    public void writeDocumentation(Appendable appendable) {
        try (MarkdownWriter writer = new MarkdownWriter(appendable)) {
            // write table headers
            writer.writeHeaders();

            commands.entrySet().stream()
                    // sort SimpleCommands by root
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(Map.Entry::getValue)
                    // get child executors
                    .map(SimpleCommand::getExecutors)
                    .flatMap(List::stream)
                    // sort on usage string
                    .sorted(Comparator.comparing(c -> c.getUsage().value()))
                    // write
                    .forEach(writer::writeCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<SimpleCommand> getCommand(String alias) {
        return Optional.ofNullable(commands.get(alias));
    }

    public List<String> suggestCommands(String alias) {
        return commands.keySet().stream().filter(s -> s.startsWith(alias)).collect(Collectors.toList());
    }

    public static SimpleCommandBus create() {
        return SimpleCommandBus.<SimpleCommand>builder()
                .owner(new Object())
                .commands(SimpleCommand::new)
                .build(SimpleCommandBus::new);
    }
}
