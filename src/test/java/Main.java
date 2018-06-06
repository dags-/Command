import block.BlockState;
import block.BlockType;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import me.dags.command.command.Input;
import me.dags.command.element.ElementFactory;
import me.dags.command.element.function.Filter;

/**
 * @author dags <dags@dags.me>
 */
public class Main extends JFrame implements KeyListener {

    public static void main(String[] args) {
        register("stone", "variant", "stone", "diorite", "andesite", "granite");
        register("wool", "color", "white", "black", "red", "blue");
        register("grass", "snowy", "false", "true");
        register("dirt", "type", "normal", "dry", "cracked", "forest");

        Input input = new Input("this is the command inp");
        input = input.replace("that");
        System.out.println(input.getRawInput());
        new Main();
    }

    private static void register(String type, String key, Object... vals) {
        new BlockType("minecraft:" + type);
        for (Object o : vals) {
            String state = String.format("minecraft:%s[%s=%s]", type, key, o);
            new BlockState(state);
        }
    }

    private final JTextField input = new JTextField();
    private final SimpleCommandBus bus;

    private int suggestion = 0;
    private List<String> suggestions = new LinkedList<>();

    private Main() {
        ElementFactory factory = ElementFactory.builder()
                .parser(BlockType.class, BlockType::get)
                .parser(BlockState.class, BlockState::get)
                .filter(BlockType.class, Filter.CONTAINS)
                .filter(BlockState.class, Filter.CONTAINS)
                .options(BlockType.class, BlockType.options())
                .options(BlockState.class, BlockState.options())
                .build();

        bus = SimpleCommandBus.<SimpleCommand>builder()
                .elements(factory)
                .commands(SimpleCommand::new)
                .build(SimpleCommandBus::new);

        bus.register(new TestCommands()).submit();

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
            Optional<SimpleCommand> command = bus.getCommand(input.peek());
            if (command.isPresent()) {
                command.get().processArguments("TheSource", input.trimFirstToken().getRawInput());
            } else {
                System.out.println("Command not found!");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void suggest(String raw) {
        try {
            if (raw.isEmpty()) {
                suggestions = bus.suggestCommands("");
                return;
            }

            Input input = new Input(raw);
            Optional<SimpleCommand> command = bus.getCommand(input.peek());
            if (command.isPresent()) {
                suggestions = command.get().suggestCommand("TheSource", input.trimFirstToken().getRawInput());
                System.out.println(suggestions);
            } else {
                suggestions = bus.suggestCommands(raw);
                System.out.println("Command not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
