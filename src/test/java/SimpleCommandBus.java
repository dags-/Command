import me.dags.command.CommandManager;
import me.dags.command.command.Command;
import me.dags.command.utils.MarkdownWriter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class SimpleCommandBus extends CommandManager<Command<Object>> {

    private final Map<String, Command<?>> commands = new HashMap<>();

    public SimpleCommandBus(Builder<Command<Object>> builder) {
        super(builder);
    }

    @Override
    protected void submit(Object owner, Command<Object> command) {
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
            writer.writeHeaders();

            commands.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(Map.Entry::getValue)
                    .distinct()
                    .map(Command::getExecutors)
                    .forEach(list -> list.stream()
                            .sorted()
                            .forEach(writer::writeCommand)
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<Command<?>> getCommand(String alias) {
        return Optional.ofNullable(commands.get(alias));
    }

    public List<String> suggestCommands(String alias) {
        return commands.keySet().stream().filter(s -> s.startsWith(alias)).collect(Collectors.toList());
    }

    public static SimpleCommandBus create() {
        return SimpleCommandBus.<Command<Object>>builder()
                .owner(new Object())
                .commands(Command::new)
                .build(SimpleCommandBus::new);
    }
}
