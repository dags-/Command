import me.dags.command.command.Command;
import me.dags.command.command.Input;
import me.dags.command.element.ChainElement;
import me.dags.command.element.ElementFactory;
import me.dags.command.element.ElementProvider;
import me.dags.command.element.function.Filter;
import me.dags.command.element.function.ValueParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public class Main extends JFrame implements KeyListener {

    public static void main(String[] args) {
        Input input = new Input("this is the command inp");
        input = input.replace("that");
        System.out.println(input.getRawInput());


        new Main();
    }

    private final JTextField input = new JTextField();
    private final SimpleCommandBus bus;

    private int suggestion = 0;
    private List<String> suggestions = new LinkedList<>();

    private static ElementProvider dependent() {
        return (id, priority, options, filter, parser) -> ChainElement.<TestEnum, String>builder()
                .dependency(TestEnum.class)
                .key(id)
                .filter(Filter.CONTAINS)
                .options(num -> Stream.of("1", "2", "3"))
                .mapper((input, num) -> num.name().toLowerCase() + "_" + ValueParser.get(int.class).parse(input))
                .build();
    }

    private Main() {
        ElementFactory factory = ElementFactory.builder()
                .provider(Object.class, dependent())
                .build();

        bus = SimpleCommandBus.<SimpleCommand>builder()
                .elements(factory)
                .commands(SimpleCommand::new)
                .build(SimpleCommandBus::new);

        bus.register(new TestCommands()).submit();

        System.out.println(bus.getDocumentation());

        input.addKeyListener(this);
        input.setPreferredSize(new Dimension(400, 30));
        input.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());

        this.add(input);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 10) {
            submit(input.getText());
            input.setText("");
            return;
        }

        if (e.getKeyCode() == 9) {
            if (suggestions.isEmpty()) {
                suggest(input.getText());
            }

            if (suggestions.isEmpty()) {
                return;
            }

            if (suggestion >= suggestions.size()) {
                suggestion = 0;
            }

            String next = suggestions.get(suggestion++);
            String current = input.getText();
            int last = current.lastIndexOf(" ") + 1;
            input.setText(current.substring(0, last) + next);
            return;
        }

        suggestions.clear();
        suggestion = 0;
    }

    private void submit(String raw) {
        try {
            Input input = new Input(raw);
            Optional<Command<?>> command = bus.getCommand(input.next());
            if (command.isPresent()) {
                command.get().processCommand(null, input.trimFirstToken().getRawInput());
            } else {
                System.out.println("Command not found!");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void suggest(String raw) {
        try {
            Input input = new Input(raw);
            Optional<Command<?>> command = bus.getCommand(input.next());
            if (command.isPresent()) {
                suggestions = command.get().suggestCommand(null, input.trimFirstToken().getRawInput());
                System.out.println(suggestions);
            } else {
                System.out.println("Command not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
